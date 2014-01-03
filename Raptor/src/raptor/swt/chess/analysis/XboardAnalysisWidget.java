/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.analysis;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.chess.util.GameUtils;
import raptor.engine.xboard.XboardEngine;
import raptor.engine.xboard.XboardInfoListener;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.ThreadService;
import raptor.service.XboardEngineService;
import raptor.swt.RaptorTable;
import raptor.swt.SWTUtils;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chess.controller.ChessBoardController;
import raptor.swt.chess.EngineAnalysisWidget;
import raptor.util.Logger;
import raptor.util.RaptorRunnable;

public class XboardAnalysisWidget implements EngineAnalysisWidget {
	private static final Logger LOG = Logger
			.getLogger(XboardAnalysisWidget.class);

	protected ChessBoardController controller;
	protected Composite composite, topLine, labelComposite;
	protected XboardEngine currentEngine;
	protected Combo engineCombo;
	protected RaptorTable bestMoves;
	protected Button startStopButton, propertiesButton;
	protected static L10n local = L10n.getInstance();
	private boolean ignoreEngineSelection;
	
	protected XboardInfoListener listener = new XboardInfoListener() {

		public void engineSentInfo(final String ply, final String score,
				final String time, final String nodes, final String pv) {
			Raptor.getInstance().getDisplay().asyncExec(
					new RaptorRunnable(controller.getConnector()) {
						@Override
						public void execute() {
							if (composite.isDisposed()) {
								return;
							}
							
							if (StringUtils.isEmpty(score))
								return;
							
							if (!currentEngine.isProcessingGo()) {
								currentEngine.send("stop");
								currentEngine.quit();
							}

							String[][] data = new String[1][5];

							data[0][0] = StringUtils.defaultString(score);
							data[0][1] = StringUtils.defaultString(ply);
							data[0][2] = StringUtils.defaultString(time);
							data[0][3] = StringUtils.defaultString(nodes);
							data[0][4] = StringUtils.defaultString(pv);
							
							bestMoves.refreshTable(data);

							/*if (StringUtils.isNotBlank(score)) {
								bestMoves.setText(0, 0, score);
							}
							if (StringUtils.isNotBlank(ply)) {
								bestMoves.setText(0, 1, ply);
							}
							if (StringUtils.isNotBlank(time)) {
								bestMoves.setText(0, 2, time);
							}

							if (StringUtils.isNotBlank(nodes)) {
								bestMoves.setText(0, 3, nodes);
							}
							if (StringUtils.isNotBlank(pv)) {
								bestMoves.setText(0, 4, pv);
							}*/
						}
					});

		}
	};

	public void clear() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Clearing XboardAnalysisWidget table");
		}
		Raptor.getInstance().getDisplay().asyncExec(
				new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						bestMoves.clearTable();
					}
				});
	}

	public Composite create(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		composite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (currentEngine != null) {
					currentEngine.quit();
				}
			}
		});

		topLine = new Composite(composite, SWT.LEFT);
		topLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginLeft = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginHeight = 1;
		rowLayout.marginWidth = 2;
		rowLayout.spacing = 0;
		topLine.setLayout(rowLayout);

		topLine.setLayout(new RowLayout());

		engineCombo = new Combo(topLine, SWT.DROP_DOWN | SWT.READ_ONLY);
		engineCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ignoreEngineSelection) {
					return;
				}

				final String value = engineCombo.getText();
				if (LOG.isDebugEnabled()) {
					LOG.debug("engineCombo value selected: " + value);
				}

				if (currentEngine != null) {
					if (currentEngine.getEngineName().equals(value)) {
						return;
					}

					final XboardEngine engineToQuit = currentEngine;
					ThreadService.getInstance().run(new Runnable() {
						public void run() {
							engineToQuit.quit();
						}
					});

				}
				startStopButton.setText(local.getString("uciAnalW_7"));
				ThreadService.getInstance().run(new Runnable() {
					public void run() {
						try {
							currentEngine = XboardEngineService.getInstance()
									.getXboardEngine(value);
							if (LOG.isDebugEnabled()) {
								LOG.debug("Changing engine to : "
										+ currentEngine.getEngineName());
							}
							//start();
						} catch (Throwable t) {
							LOG.error("Error switching chess engines", t);
						}
					}
				});
			}
		});

		labelComposite = new Composite(topLine, SWT.NONE);
		labelComposite.setLayout(SWTUtils
				.createMarginlessRowLayout(SWT.VERTICAL));

		startStopButton = new Button(topLine, SWT.FLAT);
		startStopButton.setText(local.getString("uciAnalW_7"));
		startStopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (startStopButton.getText().equals(local.getString("uciAnalW_32"))) {
					start();
					startStopButton.setText(local.getString("uciAnalW_7"));
				} else {
					stop();
					startStopButton.setText(local.getString("uciAnalW_32"));
				}
			}
		});

		bestMoves = new RaptorTable(composite, SWT.BORDER | SWT.FULL_SELECTION,
				false, true);
		bestMoves.setToolTipText(local.getString("uciAnalW_37"));
		bestMoves.addColumn(local.getString("uciAnalW_38"), SWT.LEFT, 10, false, null);
		bestMoves.addColumn(local.getString("uciAnalW_39"), SWT.LEFT, 10, false, null);
		bestMoves.addColumn(local.getString("uciAnalW_40"), SWT.LEFT, 10, false, null);
		bestMoves.addColumn(local.getString("uciAnalW_41"), SWT.LEFT, 10, false, null);
		bestMoves.addColumn(local.getString("uciAnalW_42"), SWT.LEFT, 60, false, null);
		bestMoves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		
		bestMoves.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, final String[] rowData) {
				Menu menu = new Menu(XboardAnalysisWidget.this.composite
						.getShell(), SWT.POP_UP);
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(local.getString("uciAnalW_43"));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Clipboard clipboard = new Clipboard(composite.getDisplay());
						String text = GameUtils.removeUnicodePieces(rowData[4]);
						TextTransfer textTransfer = TextTransfer.getInstance();
						Transfer[] transfers = new Transfer[] { textTransfer };
						Object[] data = new Object[] { text };
						clipboard.setContents(data, transfers);
						clipboard.dispose();
					}
				});
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!composite.getDisplay().readAndDispatch()) {
						composite.getDisplay().sleep();
					}
				}
				menu.dispose();
			}
		});

		updateEnginesCombo();
		// updateCustomButtons();

		updateFromPrefs();
		return composite;
	}

	public ChessBoardController getChessBoardController() {
		return controller;
	}

	public Composite getControl() {
		return composite;
	}

	public void onShow() {
		//clear();
		updateEnginesCombo();
		start();
		composite.layout(true, true);
	}

	public void quit() {
		Raptor.getInstance().getDisplay().asyncExec(
				new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						if (!composite.isDisposed()) {
							clear();
						}
					}
				});
		if (currentEngine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					currentEngine.quit();
				}
			});
		}
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void start() {
		if (!composite.isVisible())
			return;
		
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				if (LOG.isDebugEnabled()) {
					LOG.debug("In XboardAnalysisWidget.start("
							+ currentEngine.getEngineName() + ")");
				}
				try {
					if (!currentEngine.isConnected()) {
						currentEngine.connect();
					}
				/*	Raptor.getInstance().getDisplay().asyncExec(
							new RaptorRunnable(controller.getConnector()) {
								@Override
								public void execute() {
									clear();
								}
							});*/
					currentEngine.stop();
					currentEngine.newGame(controller.getGame().getVariant());
					currentEngine.setPosition(controller.getGame().toFen(),
							controller.getGame().isWhitesMove());
					currentEngine.analyze(listener);
					Raptor.getInstance().getDisplay().asyncExec(
							new RaptorRunnable() {
								@Override
								public void execute() {
									startStopButton.setText(local.getString("uciAnalW_7"));
								}
							});

				} catch (Throwable t) {
					LOG.error("Error starting engine", t);
				}
			}
		});
	}

	public void stop() {
		if (currentEngine != null) {
			ThreadService.getInstance().run(new Runnable() {
				public void run() {
					if (currentEngine.isConnected()) {
						currentEngine.stop();
						Raptor.getInstance().getDisplay().asyncExec(
								new RaptorRunnable() {
									@Override
									public void execute() {
										startStopButton.setText(local.getString("uciAnalW_32"));
									}
								});
					}
				}
			});
		}
	}

	public void updateFromPrefs() {
		Color background = Raptor.getInstance().getPreferences().getColor(
				PreferenceKeys.BOARD_BACKGROUND_COLOR);
		composite.setBackground(background);
		topLine.setBackground(background);
		labelComposite.setBackground(background);
	}

	public void updateToGame() {
		if (startStopButton.getText().equals(local.getString("uciAnalW_7"))) {
			start();
		}
	}
	
	protected void updateEnginesCombo() {
		ignoreEngineSelection = true;
		engineCombo.removeAll();
		XboardEngine[] engines = XboardEngineService.getInstance().getXboardEngines();
		for (XboardEngine engine : engines) {
			engineCombo.add(engine.getEngineName());
		}

		XboardEngine defaultEngine = XboardEngineService.getInstance()
				.getDefaultEngine();
		if (defaultEngine != null) {
			for (int i = 0; i < engineCombo.getItemCount(); i++) {
				if (engineCombo.getItem(i).equals(defaultEngine.getEngineName())) {
					currentEngine = engines[i].getDeepCopy();
					engineCombo.select(i);
					break;
				}
			}
		}
		ignoreEngineSelection = false;
		topLine.pack(true);
		topLine.layout(true, true);
	}

}
