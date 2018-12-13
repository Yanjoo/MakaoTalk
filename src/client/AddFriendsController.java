package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AddFriendsController implements Initializable {
	
	private @FXML Button btnAdd;
	private @FXML TextField id;

	private ListView friendsList;
	
	private FileInputStream fi;

	public void setList(ListView friendsList) {
		this.friendsList = friendsList;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnAdd.setOnAction(e -> addFriend());
		
		id.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
					addFriend();
				}
			}
			
		});
	}
	
	public void addFriend() {
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress("localhost", 5001));
			String friendName = id.getText();
			Client client = Client.getClient();
			String myName = client.getId();
			
			byte[] addFriend = ("A/" + friendName + "/" + myName).getBytes("UTF-8");
			
			// 서버에 아이디와 비밀번호를 전송
			OutputStream os = socket.getOutputStream();
			os.write(addFriend);
			os.flush();
			
			byte[] addResult = new byte[10];
			InputStream is = socket.getInputStream();
			int readByteCount = is.read(addResult);
			String result = new String(addResult, 0, readByteCount, "utf-8");
			System.out.println("친구 추가 결과: " + result);
			
			if (result.equals("fail")) throw new Exception();
			
			client.addFriends(friendName);
			
			try {
				fi = new FileInputStream("C:/Temp/profile_image/" + friendName + ".gif");
			} catch (FileNotFoundException e) {
				try {
					fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			
			Platform.runLater(() -> {
				ImageView profileImage = new ImageView(new Image(fi));
				profileImage.setFitHeight(32);
				profileImage.setFitWidth(32);
				Label name = new Label(friendName);
				
				HBox x = new HBox();
				x.setSpacing(10);
				x.getChildren().addAll(profileImage, name);
				
				friendsList.getItems().add(x);
			});
			socket.close();
			
		} catch (Exception e1) {
			System.err.println("친구 추가 실패");
			try {
				socket.close();
			} catch (IOException e2) {}
			Stage primaryStage = (Stage) btnAdd.getScene().getWindow();
			primaryStage.close();
			return;
		}
	
		Stage primaryStage = (Stage) btnAdd.getScene().getWindow();
		primaryStage.close();
		System.out.println("친구 추가 성공");
		
	}
}
