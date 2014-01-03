package raptor.service;

import org.eclipse.jface.dialogs.MessageDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.RaptorPreferenceStore;

public class CheckUpdates {
	private static final L10n local = L10n.getInstance();

	private static final int appVersion[] = RaptorPreferenceStore.APP_VERSION;
	private static String lastVersion = null;

	//private static final String updUrl = "http://dl.dropbox.com/u/46373738/upd";
	private static final String updUrl = null;
	
	public static void checkUpdates() {
		if (Raptor.getInstance().getPreferences().getBoolean("ready-to-update")) {
			Raptor.getInstance().getPreferences().setValue("ready-to-update", "false");
			return;
		}
		
		if (!Raptor.getInstance().getPreferences().getString("app-version")
				.equals(RaptorPreferenceStore.getVersion()))
			Raptor.getInstance().getPreferences().setValue("app-version", RaptorPreferenceStore.getVersion());

                boolean isNewerVersion = false;
		try {
			URL updateUrl = new URL(updUrl);
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					updateUrl.openStream()), 1024);
			String lastVersionLine = bin.readLine();
			short newVersionData[] = new short[] {0, 0, 0, 0};
			Pattern p = Pattern.compile("(\\d+)\\.(\\d+)(?:u(\\d+)(?:f(\\d+))?)?");
			Matcher m = p.matcher(lastVersionLine);
			if (m.matches()) {
	                        MatchResult r = m.toMatchResult();
                                lastVersion = r.group();
	                        for (int i=1; i<=r.groupCount(); i++)
	                            newVersionData[i] = Short.parseShort(m.group(i));
                        } else {
                            return;
                        }
			for (int i = 0; i < 4; i++) {
				if (appVersion[i] < newVersionData[i]) {
					isNewerVersion = true;
					break;
				} else if (appVersion[i] > newVersionData[i])
					break;
                        }
			bin.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("checkUpdates " + isNewerVersion);
			if (isNewerVersion) {
				Raptor.getInstance().getPreferences().setValue("ready-to-update", "true");
				Raptor.getInstance().getDisplay().asyncExec (new Runnable () {
					@Override
					public void run() {
						MessageDialog.openInformation(
								Raptor.getInstance().getDisplay().getActiveShell(),
								local.getString("newVersion"),
								local.getString("newVersAvail", lastVersion));
					}
				});				
			}			
                }
	}
}
