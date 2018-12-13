package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ClientMain extends Application {
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// 컨트롤러 클래스에게 primaryStage를 넘겨주기 위한 방법
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
		
		// 루트 컨테이너, 기본 레이아웃? 이라고 해야하나, 가장 아래 쪽 레이아웃
		Parent root = loader.load();
		// FMLLoader의 gerController 메소드는 fxml 파일에 fx:controller로 지정되어있는 클래스를 리턴한다
		// primaryStage를 LoginController에게 넘겨주기 위해 사용한다(dialog를 처리할 때 필요)
		LoginController loginController = loader.getController();
		loginController.setPrimaryStage(primaryStage);
		
		Client client = Client.getClient();
		
		
		// 장면 생성, 하나의 무대엔 한개의 장면만 보여 줄 수 있다
		Scene scene = new Scene(root);
		
		// 현재 무대(primaryStage의 설정)
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setTitle("마카오톡");
		
		primaryStage.getIcons().add(new Image(getClass().getResource("images/icon.jpg").toString()));
		// show를 해야 보여줌
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	

}
