package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RoomsController implements Initializable {

	@FXML Button showFriends;
	@FXML ListView chatRoomList;
	@FXML Label receiveInfo;
	
	private Stage primaryStage;
	private Scene firstScene;
	private List<String> runningChatRoom;	
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public void setScene(Scene primaryScene) {
		firstScene = primaryScene;
	}
	
	public void setRunningRoom(List<String> runningChatRoom) {
		this.runningChatRoom = runningChatRoom;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		showFriends.setOnAction(e -> showFriendList());
		
		Iterator<String> iter = Client.rooms.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			Label roomName = new Label(name);
			roomName.setFont(new Font("NanumGothic", 12));
			
			
			FileInputStream fi = null;
			try {
				fi = new FileInputStream("C:/Temp/profile_image/" + name + ".gif");
			} catch (FileNotFoundException e) {
				try {
					fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
				} catch (FileNotFoundException e1) {}
			}
			
			Client client = Client.getClient();
			String contents = null;
			try {
				File chatData = new File("C:/Temp/" + client.getId() + "To" + name + ".txt");
				System.out.println("C:/Temp/" + client.getId() + "To" + name + ".txt");
			
				FileReader fr = new FileReader(chatData);
				int readCharNo;
				char[] cbuf = new char[30];
				char[] read = new char[1];
				
				int index = 0;
				
				while ((readCharNo=fr.read(read)) != -1) {
					if (read[0] == '\n') {
						index = 0;
						String data = new String(cbuf);
						//System.out.println("읽은 대화 내용: " + data);
						
						String[] datas = data.split("/");
						//String from = datas[0];
						contents = datas[1];
					} else {
						cbuf[index++] = read[0];
					}
				}
			} catch(Exception e) {
				System.err.println(name + " 파일 없음");
			}
			
			Label text = new Label(contents);
			
			VBox v = new VBox();
			v.setSpacing(5);
			v.getChildren().addAll(roomName, text);
			
			ImageView profileImage = new ImageView(new Image(fi));
            profileImage.setFitHeight(50);
            profileImage.setFitWidth(50);

			HBox x = new HBox();
			x.setSpacing(10);
			x.getChildren().addAll(profileImage, v);
						
			x.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getClickCount() > 1) {
						Iterator<String> iter = runningChatRoom.iterator();
						while (iter.hasNext()) {
							String Name = iter.next();
							if (Name.equals(roomName.getText())) {
								System.err.println(Name + " 방이 이미 열려 있음");
								return;
							}
						}
						
						System.out.println(roomName.getText() + " 대화 방 열기");
						try {
							FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
							
							Parent root = loader.load();
							ChatController chatControl = loader.getController();
							
							System.out.println(roomName.getText() + " 방 선택");
										
							Scene scene = new Scene(root);
							scene.getStylesheets().add(getClass().getResource("css/chatList.css").toString());
							Stage chatStage = new Stage();
							chatStage.setScene(scene);
							chatStage.setTitle(roomName.getText());
							chatStage.getIcons().add(new Image(getClass().getResource("images/icon.jpg").toString()));
							chatStage.setX(primaryStage.getX() + Math.random() * 200 - Math.random() * 200);
							chatStage.setY(primaryStage.getY() + Math.random() * 200 - Math.random() * 200);
							chatStage.setOnCloseRequest(e -> {
							Iterator<String> iter2 = runningChatRoom.iterator();
								while (iter2.hasNext()) {
									if (iter2.next() == roomName.getText()) {
										iter2.remove();
									}
								}
							});
							chatStage.setResizable(false);
							chatStage.show();
							chatControl.setName(roomName.getText());
							// 대화 내용 불러오기
							chatControl.loadChat(); 
							
							runningChatRoom.add(roomName.getText());
					} catch (IOException e) {
						e.printStackTrace();
					}
					}
				}
				
			});
			
			chatRoomList.getItems().add(x);
		}
	
		
	}

	private void showFriendList() {
		System.out.println("친구 목록을 보여줍니다");
		primaryStage.setScene(firstScene);
		
	}

	

	

	

}
