package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatController implements Initializable {
	
	private @FXML Label friendName;
	private @FXML ListView chatList;
	private @FXML TextArea message;
	private @FXML ImageView friendProfile;
	
	private String toName;
	private String fromName;
	
	private Client client;

	private FileInputStream fi;
	
	private Image image;
	
	// FriendsController setMessage에서 쓰임 
	public ListView getChatList() {
		return chatList;
	}
	
	// 대화 상대의 이름 설정
	public void setName(String name) {
		System.err.println("설정 이름: " + name);
		this.toName = name;
		
		try {
			fi = new FileInputStream("C:/Temp/profile_image/" + toName + ".gif");
		} catch (FileNotFoundException e) {
			try {
				fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
			} catch (FileNotFoundException e1) {}
		}
		
		Platform.runLater(() -> {
			friendProfile.setImage(new Image(fi));
			friendName.setText(toName);
			
		});

	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Client client = Client.getClient();
		fromName = client.getId();

		message.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
					btnSend();
				}
			}
		});
	}
	
	// 대화 내용 불러오기, 만들어야 함
	public void loadChat() {
		File chatData = new File("C:/Temp/" + toName.trim() + "To" + fromName.trim() + ".txt");
		try {
			FileReader fr = new FileReader(chatData);
			int readCharNo;
			char[] cbuf = new char[30];
			char[] read = new char[1];
			
			int index = 0;
			
			while ((readCharNo=fr.read(read)) != -1) {
				if (read[0] == '\n') {
					index = 0;
					String data = new String(cbuf);
					System.out.println("읽은 대화 내용: " + data);
					
					String[] datas = data.split("/");
					String from = datas[0];
					String contents = datas[1];
						
					if (from.equals(fromName)) {
						client = Client.getClient();
						Platform.runLater(() -> {
							ImageView profileImage = new ImageView(client.getProfileImage());
			                profileImage.setFitHeight(32);
			                profileImage.setFitWidth(32);
			                
			                Label myName = new Label(fromName);
			                Label text = new Label(contents);
							
			                VBox v = new VBox();
			                v.setSpacing(5);
			                v.setAlignment(Pos.TOP_RIGHT);
			                v.getChildren().addAll(myName, text);
			                
							HBox x = new HBox();
							x.setMaxWidth(chatList.getWidth() - 20);
							x.setAlignment(Pos.TOP_RIGHT);
							x.setSpacing(10);
							x.getChildren().addAll(v, profileImage);
							chatList.getItems().add(x);
						});
					} else {
						Platform.runLater(() -> {
							try {
								fi = new FileInputStream("C:/Temp/profile_image/" + toName + ".gif");
							} catch (FileNotFoundException e) {
								try {
									fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
								} catch (FileNotFoundException e1) {}
							}
							
							ImageView profileImage = new ImageView(new Image(fi));
			                profileImage.setFitHeight(32);
			                profileImage.setFitWidth(32);
			                
			                Label friendName = new Label(from);
			                Label text = new Label(contents);
							
			                VBox v = new VBox();
			                v.setSpacing(5);
			                v.getChildren().addAll(friendName, text);
			                
							HBox x = new HBox();
							x.setMaxWidth(chatList.getWidth() - 20);
							x.setSpacing(10);
							x.getChildren().addAll(profileImage, v);
							chatList.getItems().add(x);
						});
					}
				} else {
					cbuf[index++] = read[0];
				}
			}
			
			Platform.runLater(() -> {
				Label label = new Label("여기까지 읽었습니다");
				Separator sep1 = new Separator();
				sep1.setPrefWidth(100);
				Separator sep2 = new Separator();
				sep2.setPrefWidth(100);
				HBox x = new HBox();
				x.setMaxWidth(chatList.getWidth() - 20);
				x.setAlignment(Pos.CENTER);
				x.setSpacing(20);
				x.getChildren().addAll(sep1, label, sep2);
				chatList.getItems().add(x);
			});
		} catch (Exception e) {
			System.err.println("파일이 존재 하지 않음 ");
			//e.printStackTrace();
		}
	}
	
	// 메시지 전송, 이모티몬 시도 해보기
	public void btnSend() {
		Client client = Client.getClient();
		String send = message.getText();
		
		// Task 클래스는 작업 후 결과물을 리턴 여기선 HBox를 리턴, HBox 안에 메시지 내용과 프로필 사진을 추가하고 이 HBox를 ListView에 보여준다, HBox는 요소들을 우정렬 좌정렬 가능
		Task<HBox> myMessage = new Task<HBox>() {
			@Override
			protected HBox call() throws Exception {
				ImageView profileImage = new ImageView(client.getProfileImage());
                profileImage.setFitHeight(32);
                profileImage.setFitWidth(32);
                
                Label myName = new Label(client.getId());
                Label text = new Label(send);
				
				VBox v = new VBox();
				v.setAlignment(Pos.TOP_RIGHT);
                v.setSpacing(5);
                v.getChildren().addAll(myName, text);
				
				HBox x = new HBox();
				x.setMaxWidth(chatList.getWidth() - 20);
				x.setAlignment(Pos.TOP_RIGHT);
				x.setSpacing(10);
				x.getChildren().addAll(v, profileImage);
				return x;
			}
			
			@Override
			protected void succeeded() {
				chatList.getItems().add(getValue());
			}
		};
		Thread thread = new Thread(myMessage);
		thread.setDaemon(true);
		thread.start();
		
		// 데이터를 실제 상대에게 보낸다
		try {
			// 클라이언트의 소켓을 얻는다
			Socket socket = client.getSocket();
			OutputStream os = socket.getOutputStream();
			System.out.println("내용: " + send);
			byte[] data = (toName.trim() + "/" + send.trim() + "/" + fromName.trim()).getBytes("utf-8");
			os.write(data);
			os.flush();
			System.out.println("메시지 전송 선공 ");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("메시지 전송 실패");
		}
		
		// 텍스트박스를 비워준다
		message.clear();
		
		try {
			File chatData = new File("C:/Temp/" + fromName.trim() + "To" + toName.trim() + ".txt");
			FileWriter fw = new FileWriter(chatData, true);
			
			fw.write(fromName.trim() + "/" + send.trim() +"/\r\n");
			fw.flush();
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 받은 메시지를 대화방에 보여준다
	public void receive(String from, String receivedMessage) {
		Task<HBox> otherMessage = new Task<HBox>() {
			@Override
			protected HBox call() throws Exception {
				Label text = new Label();
				text.setText(receivedMessage);
				System.out.println("받은 데이터: " + text.getText());
				
				try {
					fi = new FileInputStream("C:/Temp/profile_image/" + from + ".gif");
				} catch (FileNotFoundException e) {
					try {
						fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
					} catch (FileNotFoundException e1) {}
				}
				
				ImageView profileImage = new ImageView(new Image(fi));
	            profileImage.setFitHeight(32);
	            profileImage.setFitWidth(32);         
				
	            Label name = new Label(friendName.getText());
	            
				VBox v = new VBox();
                v.setSpacing(5);
                v.getChildren().addAll(name, text);
				
				HBox x = new HBox();
				x.setMaxWidth(chatList.getWidth() - 20);
				x.setSpacing(10);
				x.getChildren().addAll(profileImage, v);
				fi.close();
				
				return x;
			}	
			
			@Override
			protected void succeeded() {
				chatList.getItems().add(getValue());
			}
		};
		
		Thread thread = new Thread(otherMessage);
		thread.setDaemon(true);
		thread.start();
		
		System.out.println(fromName);
		System.out.println(toName + "to" + fromName);
		try {
			File chatData1 = new File("C:/Temp/" + fromName.trim() + "To" + toName.trim() + ".txt");
			FileWriter fw = new FileWriter(chatData1, true);
			
			fw.write(from + "/" + receivedMessage.trim() +"/\r\n");
			fw.flush();
			
			fw.close();
			
		} catch (Exception e) {}
	}

}
