package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FriendsController implements Initializable {

	private @FXML AnchorPane ap;
	private @FXML Button showTalk;
	private @FXML Button addFriends;
	private @FXML ListView myProfile;
	private @FXML ListView friendsList;
	private @FXML Label receiveInfo;
	
	private ImageView profileImage;
	
	// 리스트뷰에서 선택된 아이템
	private Label clickedItem;
	private String clickedName;
	//private List<String> rooms = new Vector<String>();
	
	// 받은 데이터
	private String receivedMessage;
	
	// LoginController에서 만든 newStage(friends.fxml의 window)
	private Stage primaryStage;
	private Scene primaryScene;
	
	private ChatController chatControl;
	
	// 수신 팝업에 좌표 조절
	private Location bottomRight = null;
	
	private List<String> runningChatRoom = new Vector<>();
	
	public void setClickedname(String text) {
		this.clickedName = text;
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	// 받은 메시지를 대화방에 출력
	public void setMessage(String from, String receivedMessage) {
		chatControl.receive(from, receivedMessage);
	}
	
	public void setScene(Scene scene) {
		primaryScene = scene;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 시작 시 프로필과 친구들 설정 -> 프로필 사진 추가로 수정
		Client client = Client.getClient();

		List friends = client.friends;
		
		showTalk.setOnAction(e -> showTalkList());
		
		profileImage = new ImageView(client.getProfileImage());
        profileImage.setFitHeight(32);
        profileImage.setFitWidth(32);
        Label label = new Label(client.getId());
		HBox x = new HBox();
		x.setSpacing(10);
		x.getChildren().addAll(profileImage, label);
		
		myProfile.setItems(FXCollections.observableArrayList(x));
		friendsList.setItems(FXCollections.observableArrayList(friends));
		
		addFriends.setOnAction(e -> addFriends());
        // 프로필 이미지 변경, 종료하고 다시 켜야 변경 됨
        profileImage.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() > 0) {
					try {
						// 파일 탐색기 열기
						FileChooser fileChooser = new FileChooser();
						fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.gif"));
						File selectedFile = fileChooser.showOpenDialog(primaryStage);
						String selectedPath = selectedFile.getPath();
						System.out.println(selectedPath); 
						
						// 선택한 파일의 경로를 얻고
						int readByteNo;
						byte[] readBytes = new byte[100];
					
						// 로그인 할 때 사용하는 profile_image 폴더에도 복사한다
						FileInputStream fis	= new FileInputStream(selectedPath.replace("\\", "/"));
						FileOutputStream fos = new FileOutputStream("C:/Temp/profile_image/" +  client.getId() + ".gif");
						while ((readByteNo = fis.read(readBytes)) != -1) {
							fos.write(readBytes, 0, readByteNo);
						}
						fos.flush();	
						fos.close();
						// 현재 계정에 이미지를 바꿔 준다
						client.setProfileImage("C:/Temp/profile_image/" +  client.getId() + ".gif");
						fis.close();	
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					Task<HBox> myMessage = new Task<HBox>() {
						@Override
						protected HBox call() throws Exception {
							profileImage = new ImageView(client.getProfileImage());
			                profileImage.setFitHeight(32);
			                profileImage.setFitWidth(32);
			                
			                Label label = new Label(client.getId());

							HBox x = new HBox();
							x.setSpacing(10);
							x.getChildren().addAll(profileImage, label);
							return x;
						}
						
						@Override
						protected void succeeded() {
							System.out.println(client.getId() + " 프로필 변경 성공");
							myProfile.getItems().remove(0);
							myProfile.setItems(FXCollections.observableArrayList(getValue()));
						}
					};
					Thread thread = new Thread(myMessage);
					thread.setDaemon(true);
					thread.start();
				}
			}
        });
	}

	private void showTalkList() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Rooms.fxml"));
			Parent talkList = loader.load();
			
			RoomsController roomControl = loader.getController();
			roomControl.setPrimaryStage(primaryStage);
			roomControl.setScene(primaryScene);
			roomControl.setRunningRoom(runningChatRoom);
			
			Scene scene = new Scene(talkList);
			primaryStage.setScene(scene);
		} catch (IOException e) {
			System.out.println("로드 실패");
			e.printStackTrace();
		}
		Client client = Client.getClient();
		client.setrooms();
	}

	// 친구추가
	public void addFriends() {
		Stage dialog = new Stage(StageStyle.UTILITY);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(primaryStage);
		dialog.setTitle("친구 추가");
			
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AddFriends.fxml"));
				
			Parent parent = loader.load();
				
			AddFriendsController addFriends = loader.getController();
			addFriends.setList(friendsList);
				
			Button btnClose = (Button)parent.lookup("#btnClose");
			btnClose.setOnAction(e->dialog.close());
				
			Scene scene = new Scene(parent);
			dialog.setScene(scene);
			dialog.setX(primaryStage.getX() + Math.random() * 50);
			dialog.setY(primaryStage.getY() + Math.random() * 50);
			dialog.show();
		} catch (Exception e) {	}	
	}
	
	
	public static String Audioname ="C:/Temp/MatalkAlert.wav";
	public static File Audiofile = new File(Audioname);
	public void AlertSound(float vol, boolean repeat) {
	    try {
	       final Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
	       clip.open(AudioSystem.getAudioInputStream(Audiofile));
	         
	       clip.addLineListener(new LineListener() {
	               @Override public void update(LineEvent event) {
	                  if(event.getType()==LineEvent.Type.STOP) {
	                     clip.close();
	                  }
	               }
	            });
	         
       FloatControl volume = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
       volume.setValue(vol);
       clip.start();
       if(repeat)
          clip.loop(Clip.LOOP_CONTINUOUSLY);
	         
	   } catch(Exception e) {
		   e.printStackTrace();	
         }
	}

	
	// 메시지 수신시 발생하는 UI 변경 작업, 팝업 처리
	public void receive(String from, String contents) {
		Platform.runLater(() ->  {
			// 아래에 수신 정보 출력
			receiveInfo.setText(from + "님 에게서 메시지 도착");
		});
		// 채팅 방이 실행 중이라면 팝업을 띄우지 않는다
		Iterator<String> iter = runningChatRoom.iterator();
		while (iter.hasNext()) {
			String room = iter.next().trim();
			System.out.println(room + " " + from);
			if (room.equals(from)) {
				return;
			}
		}
		AlertSound(6, false);
		
		Platform.runLater(() ->  {
			// 팝업 생성 -> 대화방이 켜져 있으면 팝업 생성 안하도록 수정
			Popup alarm = new Popup();
			try {
				Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		        double x = screenBounds.getMinX() + screenBounds.getWidth() - ap.getPrefWidth() - 2;
		        double y = screenBounds.getMinY() + screenBounds.getHeight() - ap.getPrefHeight() - 2;
		        
		        Location bottomRight = new Location(x,y);
		        
				Parent parent = FXMLLoader.load(getClass().getResource("Alarm.fxml"));
				// 팝업에 미리보기 제공
				ImageView image = (ImageView) parent.lookup("#image");
				FileInputStream fi = null;
				try {
					fi = new FileInputStream("C:/Temp/profile_image/" + from + ".gif");
				} catch (FileNotFoundException e) {
					try {
						fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
					} catch (FileNotFoundException e1) {}
				}
				
				image.setImage(new Image(fi));
				fi.close();
				Label lbl = (Label) parent.lookup("#fromName");
				lbl.setText(from);
				lbl = (Label) parent.lookup("#contents");
				lbl.setText(contents);
				// 팝업을 누르면 대화방 생성 -> 대화방이 켜져 있으면 그걸로 이동으로 수정
				AnchorPane popup = (AnchorPane) parent.lookup("#alarm");
				popup.setOnMouseClicked(e -> {
					chatStart(from);
					alarm.hide();
					runningChatRoom.add(from);
				});
				alarm.getContent().add(parent);
				alarm.setAutoHide(true);
				alarm.show(primaryStage, bottomRight.getX(), bottomRight.getY());
			} catch (IOException e1) {}
		});
	}
	
	// 대화 방 시작
	public void chatStart(String clickedName) {
			Iterator<String> iter = runningChatRoom.iterator();
			Client client = Client.getClient();
			
			while (iter.hasNext()) {
				String name = iter.next();
				System.out.println("rooms: " + name);
				if (name.equals(clickedName)) {
					System.err.println("대화 방이 이미 존재");
					return;
				}
			}
		
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
				
				Parent root = loader.load();
				chatControl = loader.getController();
				
				System.out.println(clickedName + " 방 선택");
							
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("css/chatList.css").toString());
				Stage chatStage = new Stage();
				chatStage.setScene(scene);
				chatStage.setTitle(clickedName);
				chatStage.getIcons().add(new Image(getClass().getResource("images/icon.jpg").toString()));
				chatStage.setX(primaryStage.getX() + Math.random() * 200 - Math.random() * 200);
				chatStage.setY(primaryStage.getY() + Math.random() * 200 - Math.random() * 200);
				chatStage.setOnCloseRequest(e -> {
				Iterator<String> iter2 = runningChatRoom.iterator();
					while (iter2.hasNext()) {
						if (iter2.next() == clickedName) {
							iter2.remove();
						}
					}
				});
				chatStage.setResizable(false);
				chatStage.show();
				chatControl.setName(clickedName);
				// 대화 내용 불러오기
				chatControl.loadChat(); 
				
				runningChatRoom.add(clickedName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 클라이언트의 대화 목록 추가
		Client.rooms.add(clickedName);
			
		try {
			Socket socket = new Socket();			
			socket.connect(new InetSocketAddress("localhost", 5001));
			
			byte[] roomAdd = ("C/" + client.getId() + "/" + clickedName).getBytes("UTF-8");
			
			OutputStream os = socket.getOutputStream();
			os.write(roomAdd);
			os.flush();
			System.out.println("대화 방 추가");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
