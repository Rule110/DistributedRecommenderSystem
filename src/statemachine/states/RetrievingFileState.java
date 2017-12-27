package statemachine.states;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import content.recommend.Recommendation;
import content.retrieve.RetrievedContent;
import filemanagement.core.FileConstants;
import filemanagement.fileretrieval.RetrievedFile;
import gui.core.GUI;
import gui.core.SceneContainerStage;
import gui.utilities.GUIText;
import javafx.concurrent.Task;
import peer.core.ViewerToUIChannel;
import statemachine.core.StateMachine;
import statemachine.utils.StateName;

public class RetrievingFileState extends State {
	StateMachine stateMachine;
	SceneContainerStage sceneContainerStage;
	GUI gui;
	private ViewerToUIChannel viewer;
	
	public RetrievingFileState(StateMachine stateMachine, SceneContainerStage sceneContainerStage, GUI gui, ViewerToUIChannel viewer) {
		this.stateMachine = stateMachine;
		this.sceneContainerStage = sceneContainerStage;
		this.gui = gui;
		this.viewer = viewer;
	}

	@Override
	public void execute(StateName param) {
		sceneContainerStage.changeScene(gui.getFileRetrievalScene());
		sceneContainerStage.setTitle(GUIText.FILE_RETRIEVAL);
		
		switch (param) {
			case RETRIEVING_FILE:
				retrieveFileAndOpen();
				break;
			default:
				break;
		}
	}
	
	private void retrieveFileAndOpen() {
		RetrievedFile retrievedFile = new RetrievedFile();
		
		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws Exception {
			    RetrievedContent retrievedContent;
			    Recommendation recommendation = gui.getDashBoardScene().getListView().getSelectionModel().getSelectedItem();
			    viewer.requestContent(recommendation);
			    retrievedContent = viewer.getRetrievedContent();
			    String filename = retrievedContent.getContent().getFileName();
			    String fileFormat = retrievedContent.getContent().getFileFormat();
			    filename += "." + fileFormat;
			    File file = new File(filename);
			    retrievedFile.setFile(file);
				return null;
			}
		};
		task.setOnSucceeded(e -> {
			try {
				openFile(retrievedFile);
				rateFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				}
		});
		new Thread(task).start();
	}
	
	private void openFile(RetrievedFile retrievedFile) throws IOException {
		if (!Desktop.isDesktopSupported()) {
			System.err.println(FileConstants.DESKTOP_NOT_SUPPORTED);
			return;
		}
		
		Desktop desktop = Desktop.getDesktop();
		if (retrievedFile.getFile().exists()) {
			desktop.open(retrievedFile.getFile());
		}
	}
	
	private void rateFile() {
		stateMachine.setCurrentState(StateName.RATING.toString());
		stateMachine.execute(StateName.INIT);
	}
}
