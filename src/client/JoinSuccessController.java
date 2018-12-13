package client;

import javafx.stage.Stage;

public class JoinSuccessController {
	private Stage primaryStage;

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		
	}
	// 현재화면 종료
	public void btnConfirm() {
		primaryStage.close();
	}
}
