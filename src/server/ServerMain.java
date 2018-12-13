package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

	public static void main(String[] args) {
		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Server.fxml의 객체를 얻는다
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
		Parent root = loader.load();
		
		Scene scene = new Scene(root);
		
		primaryStage.setTitle("마카오톡 서버");
		primaryStage.setResizable(false);
		// Server.fxml의 컨트롤러 클래스를 얻는다
		ServerController serverController = loader.getController();
		// x를 누르면 서버가 멈추게 한다
		primaryStage.setOnCloseRequest(event -> serverController.serverStop());
		primaryStage.setScene(scene);
		
		primaryStage.show();
	}

}
