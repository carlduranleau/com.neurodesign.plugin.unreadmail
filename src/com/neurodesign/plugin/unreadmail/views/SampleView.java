package com.neurodesign.plugin.unreadmail.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.graphics.Point;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import lotus.domino.*;
import java.util.*;

import com.ibm.notes.java.api.data.NotesDatabaseData;
import com.ibm.notes.java.api.data.NotesDocumentData;
import com.ibm.notes.java.api.data.NotesFormData;

import com.ibm.notes.java.ui.*;
import com.neurodesign.plugin.unreadmail.Activator;
import com.neurodesign.plugin.unreadmail.utils.*;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart implements NotesEventListener {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.neurodesign.plugin.unreadmail.views.SampleView";
	private final String NEWLINE = System.getProperty("line.separator");
	
	private TableViewer viewer;
	private Action action1; //Mise à jour
	private Action action2; //Ouvrir
	private Action action3; //Marquer comme lu
	private Action action4; //Ouvrir courrier
	private Action action5; //Créer message
	private Action action6; //Supprimer message
	private Action doubleClickAction;

	private NotesEventHandler eventHandler;
	private String eventID;

	IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		/*
		 * @see IPropertyChangeListener.propertyChange()
		 */
		public void propertyChange(PropertyChangeEvent event) {

			if (event.getProperty().equals(Activator.PREF_REFRESH)) {
				SharedData.setRefreshSpeed(Integer.parseInt(Activator.getDefault().getRefreshSpeed()));
				initEventThread();
			}
			if (event.getProperty().equals(Activator.PREF_SORT)) {
				SharedData.setSortType(Activator.getDefault().getSortType());
				rebuild();
			}
		}
	};

	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
	}
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (SharedData.getMailsData() == null || SharedData.getMailsData().size() == 0) {
				Object [] rtn = {new DocDataBean("0", "","Aucun document trouvé","", null)}; 
				return rtn;
			}
			return SharedData.getMailsData().toArray();
		}
	}
	
	/**
	 * The constructor.
	 */
	public SampleView() {
		try {
			//initNotesParameters();
			initEventThread();

			if (SharedData.getMailDBServer().equals("") || SharedData.getMailDBName().equals("")) return;

		} catch (Exception e) {
			showMessage("Constructor: " + e.toString());
			e.printStackTrace();
		}
	}
	
	synchronized private void initEventThread () {
		try {
			if (eventHandler != null) eventHandler.unregisterEvent(eventID);
			
			eventHandler = new NotesEventHandler();
			
			NotesEventParameters parameters = new NotesEventParameters();
			parameters.setQuery("($All)");
			parameters.setServer(SharedData.getMailDBServer());
			parameters.setPath(SharedData.getMailDBName());
			parameters.setRefreshspeed(SharedData.getRefreshSpeed());
			try {
				eventID = eventHandler.registerEvent(this, NotesEventHandler.EVENT_TYPE_TIMERTICK, parameters);
			} catch (Exception e) {
				showMessage("Constructor.Register: " + e.toString());
			}			
			
		} catch (Exception e) {
			showMessage("Unable to initialize event thread");
		}
	}

	public void rebuild () {
		eventReceived(0, null);
	}
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		try {
			
			CellLabelProvider bodyLabelProvider = new CellLabelProvider() {

				public String getToolTipText(Object element) {
					DocDataBean bean = (DocDataBean)element;
					
					if (bean.getId().equals("")) return bean.getTitle();
					
					String content = bean.getContent();
					if (content.length() > 400)
						content = content.substring(0, 400) + " ...";
					
					String tt = "De : " + bean.getFrom() + NEWLINE; 
					
					if (!(bean.getTo().equals("")))
						tt = tt + "À : " + bean.getTo() + NEWLINE; 
					if (!(bean.getCopyTo().equals("")))
						tt = tt + "CC : " + bean.getCopyTo() + NEWLINE; 
					if (!(bean.getBlindCopyTo().equals("")))
						tt = tt + "CCC : " + bean.getBlindCopyTo() + NEWLINE; 
					
					tt = tt + "Sujet : " + bean.getTitle() + NEWLINE;
					
					tt = tt + NEWLINE + content;
					
					return  tt;
				}

				public Point getToolTipShift(Object object) {
					return new Point(5, 5);
				}

				public int getToolTipDisplayDelayTime(Object object) {
					return 500;
				}

				public int getToolTipTimeDisplayed(Object object) {
					return 60000;
				}

				public void update(ViewerCell cell) {
					cell.setText(((DocDataBean)cell.getElement()).getTitle());
				}
			};
			
			CellLabelProvider dateLabelProvider = new CellLabelProvider() {

				public String getToolTipText(Object element) {
					DocDataBean bean = (DocDataBean)element;
					
					if (bean.getId().equals("")) return bean.getTitle();
					
					String content = bean.getContent();
					if (content.length() > 400)
						content = content.substring(0, 400) + " ...";
					
					String tt = "De : " + bean.getFrom() + NEWLINE; 
					
					if (!(bean.getTo().equals("")))
						tt = tt + "À : " + bean.getTo() + NEWLINE; 
					if (!(bean.getCopyTo().equals("")))
						tt = tt + "CC : " + bean.getCopyTo() + NEWLINE; 
					if (!(bean.getBlindCopyTo().equals("")))
						tt = tt + "CCC : " + bean.getBlindCopyTo() + NEWLINE; 
					
					tt = tt + "Sujet : " + bean.getTitle() + NEWLINE;
					
					tt = tt + NEWLINE + content;
					
					return  tt;
				}

				public Point getToolTipShift(Object object) {
					return new Point(5, 5);
				}

				public int getToolTipDisplayDelayTime(Object object) {
					return 500;
				}

				public int getToolTipTimeDisplayed(Object object) {
					return 60000;
				}

				public void update(ViewerCell cell) {
					cell.setText(((DocDataBean)cell.getElement()).getDescription());
				}
			};
		    
		    viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		    ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		    
		    TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
		    column.setLabelProvider(bodyLabelProvider);
			column.getColumn().setText("Sujet");
			column.getColumn().setWidth(200);
			column = new TableViewerColumn(viewer, SWT.LEFT);
			column.getColumn().setText("Date");
			column.getColumn().setWidth(100);
			column.setLabelProvider(dateLabelProvider);
		    viewer.setContentProvider(new ViewContentProvider());
			viewer.setInput(getViewSite());
			
			// Create the help context id for the viewer's control
			PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.domiclipse.tutorial1.viewer");
			makeActions();
			hookContextMenu();
			hookDoubleClickAction();
			contributeToActionBars();
		} catch (Exception e) {
			showMessage("createPartControl: " + e.toString());
		}
	}

	private void hookContextMenu() {
		try {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					SampleView.this.fillContextMenu(manager);
				}
			});
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			viewer.getControl().setMenu(menu);
			getSite().registerContextMenu(menuMgr, viewer);
		} catch (Exception e) {
			showMessage("hookContextMenu: " + e.toString());
		}
	}

	private void contributeToActionBars() {
		try {
			IActionBars bars = getViewSite().getActionBars();
			fillLocalPullDown(bars.getMenuManager());
			fillLocalToolBar(bars.getToolBarManager());
		} catch (Exception e) {
			showMessage("contributeToActionBars: " + e.toString());
		}
	}

	private void fillLocalPullDown(IMenuManager manager) {
		try {
			manager.add(action1);
			//manager.add(new Separator());
			//manager.add(action2);
			//manager.add(new Separator());
			//manager.add(action3);
			//manager.add(new Separator());
			manager.add(action4);
			//manager.add(new Separator());
			manager.add(action5);
		} catch (Exception e) {
			showMessage("fillLocalPullDown: " + e.toString());
		}
	}

	private void fillContextMenu(IMenuManager manager) {
		try {
			manager.add(action2);
			manager.add(action3);
			manager.add(action6);
			// Other plug-ins can contribute there actions here
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(action1);
			manager.add(action4);
			manager.add(action5);
		} catch (Exception e) {
			showMessage("fillContextMenu: " + e.toString());
		}
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		try {
			manager.add(action1);
			manager.add(action4);
			manager.add(action5);
			//manager.add(action2);
			//manager.add(action3);
		} catch (Exception e) {
			showMessage("fillLocalToolBar: " + e.toString());
		}
	}

	private void makeActions() {
		try {
			action1 = new Action() {
				public void run() {
					rebuild();
				}
			};
			action1.setText("Mise à jour");
			action1.setToolTipText("Met à jour la vue");
			action1.setImageDescriptor(Activator.getImageDescriptor("icons/icon_refresh.png"));
			
			action2 = new Action() {
				public void run() {
					doubleClickAction.run();
					rebuild();
				}
			};
			action2.setText("Ouvrir le message");
			action2.setToolTipText("Ouverture du message sélectionné");
			action2.setImageDescriptor(Activator.getImageDescriptor("icons/icon_document.png"));
			
			action3 = new Action() {
				public void run() {
					ISelection selection = viewer.getSelection();
					if (selection.isEmpty()) return;
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					if (obj == null) return;
					try {
						Database mailDB = null;
					
						NotesThread.sinitThread();
						Session notesSession = NotesFactory.createSession();
						mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
						if (mailDB.isOpen()) {
							Iterator it = ((IStructuredSelection)selection).iterator();
							String unid;
							while (it.hasNext()) {
								obj = it.next();
								unid = ((DocDataBean)obj).getId();
								if (unid != null && !(unid.equals(""))) {
									Document doc = mailDB.getDocumentByUNID(unid);
									if (doc != null) {
										doc.markRead();
										doc.save();
										doc.recycle();
									} else {
										showMessage ("Impossible de modifier ce document. Vérifiez qu'il n'a pas été supprimé ou déplacé.");
									}
								}
							}
						}
						if (mailDB != null) mailDB.recycle();
						if (notesSession != null) notesSession.recycle();
					} catch (Exception e) {
						showMessage("Action: " + e.toString());
					} finally {
						NotesThread.stermThread();
					}
					rebuild();
				}
			};
			action3.setText("Marquer comme lu");
			action3.setToolTipText("Marque les messages sélectionnés comme lu");
			action3.setImageDescriptor(Activator.getImageDescriptor("icons/icon_check.png"));
			
			action4 = new Action() {
				public void run() {
					try {
						Database mailDB = null;
					
						NotesThread.sinitThread();
						Session notesSession = NotesFactory.createSession();
						mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
						if (mailDB.isOpen()) {
							NotesUIWorkspace ws = new NotesUIWorkspace(); 
							ws.openDatabase(new NotesDatabaseData(mailDB));
						}
						if (mailDB != null) mailDB.recycle();
						if (notesSession != null) notesSession.recycle();
					} catch (Exception e) {
						showMessage("Action: " + e.toString());
					} finally {
						NotesThread.stermThread();
					}
					rebuild();
				}
			};
			action4.setText("Ouvrir courrier");
			action4.setToolTipText("Ouvre la boite de courrier");
			action4.setImageDescriptor(Activator.getImageDescriptor("icons/icon-inbox.png"));

			action5 = new Action() {
				public void run() {
					try {
						Database mailDB = null;
					
						NotesThread.sinitThread();
						Session notesSession = NotesFactory.createSession();
						mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
						if (mailDB.isOpen()) {
							NotesUIWorkspace ws = new NotesUIWorkspace(); 
							ws.composeDocument(new NotesFormData(new NotesDatabaseData(mailDB), "Memo"));
						}
						if (mailDB != null) mailDB.recycle();
						if (notesSession != null) notesSession.recycle();
					} catch (Exception e) {
						showMessage("Action: " + e.toString());
					} finally {
						NotesThread.stermThread();
					}
					rebuild();
				}
			};
			
			action5.setText("Créer message");
			action5.setToolTipText("Crée un nouveau message");
			action5.setImageDescriptor(Activator.getImageDescriptor("icons/icon_write.png"));

			action6 = new Action() {
				public void run() {
					if (!(MessageDialog.openQuestion(viewer.getControl().getShell(), "Courrier non-lu", "Cette action supprimera définitivement tous les documents sélectionnés. Voulez-vous continuer?"))) return;
					ISelection selection = viewer.getSelection();
					if (selection.isEmpty()) return;
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					if (obj == null) return;
					try {
						Database mailDB = null;
						
						NotesThread.sinitThread();
						Session notesSession = NotesFactory.createSession();
						mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
						if (mailDB.isOpen()) {
							Iterator it = ((IStructuredSelection)selection).iterator();
							String unid;
							while (it.hasNext()) {
								obj = it.next();
								unid = ((DocDataBean)obj).getId();
								if (unid != null && !(unid.equals(""))) {
									Document doc = mailDB.getDocumentByUNID(unid);
									if (doc != null) {
										doc.removePermanently(true);
										if (!(doc == null)) doc.recycle();
									} else {
										showMessage ("Impossible de modifier ce document. Vérifiez qu'il n'a pas été supprimé ou déplacé.");
									}
								}
							}
						}
						if (mailDB != null) mailDB.recycle();
						if (notesSession != null) notesSession.recycle();
					} catch (Exception e) {
						showMessage("Action: " + e.toString());
					} finally {
						NotesThread.stermThread();
					}
					rebuild();
				}
			};
			action6.setText("Supprimer message");
			action6.setToolTipText("Supprime les messages sélectionnés");
			action6.setImageDescriptor(Activator.getImageDescriptor("icons/icon-delete.png"));
			
			
			doubleClickAction = new Action() {
				public void run() {
					ISelection selection = viewer.getSelection();
					if (selection.isEmpty()) return;
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					if (obj == null) return;
					try {
						Database mailDB = null;
					
						NotesThread.sinitThread();
						Session notesSession = NotesFactory.createSession();
						mailDB = notesSession.getDatabase(SharedData.getMailDBServer(), SharedData.getMailDBName());
						if (mailDB.isOpen()) {
							String unid = ((DocDataBean)obj).getId();
							if (unid != null && !(unid.equals(""))) {
								Document doc = mailDB.getDocumentByUNID(unid);
								if (doc != null) {
									NotesUIWorkspace ws = new NotesUIWorkspace(); 
									ws.openDocument(false, new NotesDocumentData(doc));
									doc.recycle();
								} else {
									showMessage ("Impossible d'ouvrir ce document. Vérifiez qu'il n'a pas été supprimé ou déplacé.");
								}
							}
						}
						if (mailDB != null) mailDB.recycle();
						if (notesSession != null) notesSession.recycle();
					} catch (Exception e) {
						showMessage("Action: " + e.toString());
					} finally {
						NotesThread.stermThread();
					}
				}
			};
		} catch (Exception e) {
			showMessage("makeActions: " + e.toString());
		}
	}

	private void hookDoubleClickAction() {
		try {
			
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					doubleClickAction.run();
					rebuild();
				}
			});
		} catch (Exception e) {
			showMessage("hookDoubleClickAction: " + e.toString());
		}
	}
	private void showMessage(String message) {
		try {
			MessageDialog.openInformation(viewer.getControl().getShell(), "Courrier non-lu", message);
		} catch (Exception e) {
			System.out.println (message);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}


	synchronized public void eventReceived(int eventType, Object obj) {
		try {
			if (viewer == null) {
				System.out.println("No viewer!");
				return;
			}
			if (!(SharedData.isRefreshing())) {
				SharedData.setRefreshFlag(true);
				RebuildThread rt = new RebuildThread(this);
				
				//Save current selection. Will be restored after the rebuild thread's end (rebuildFinished).
				SharedData.saveSelection(viewer);
				rt.start();
			}
		} catch (Exception e) {
			System.out.println ("Launching thread failed");
			showMessage("eventReceived: " + e.toString());
		}
	}
	
	//Called by the rebuild thread 
	synchronized public void rebuildFinished() {
		if (viewer == null) {
			System.out.println("No viewer!");
			return;
		}
		if (!(SharedData.getMailOwner().equals(""))) {
			setPartName(SharedData.getMailOwner() + " (" + Integer.toString(SharedData.getUnreadCount()) + ")");
		}
		viewer.refresh(false, false);
		
		//Restore the selection (Saved in the eventReceived function).
		SharedData.restoreSelection(viewer);
	}
	
	@Override
	public void dispose() {
		try {
			if (eventHandler != null)
				eventHandler.unregisterEvent(eventID);
		} catch (Exception e) {
			showMessage("dispose function: " + e.toString());
		} finally {
			super.dispose();
		}
	}
	
	/*
	
	//DISPLAY A MESSAGE IN THE NOTES STATUS BAR
	
	static public void LogToStatusLine(final String msg){
		Display display = Display.getDefault();
		display.asyncExec(new Runnable(){
			public void run() {
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				IWorkbenchPart part = page.getActivePart();
				IWorkbenchPartSite site = part.getSite();
				if (site instanceof IViewSite){
					IViewSite vSite = ( IViewSite ) site;
					IActionBars actionBars = vSite.getActionBars();
					if( actionBars == null ) return ;
					IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
					if( statusLineManager == null )	return ;
					statusLineManager.setMessage( msg);
				}
			}
		});
	}
	 
	 */
}