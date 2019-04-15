import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.event.*;
import javafx.geometry.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

public class TodoCards extends Application{
	
	static MyButton addNewButton, risePriorityButton, lowerPriorityButton,
		colorRedButton, colorOrangeButton, colorGreenButton,
		colorNeutralButton, taskFinishButton, makeBoldButton, deadlineButton, deleteButton;
	static VBox stickersPanel, controlPanel;	
	static ArrayList<Sticker> stickers = new ArrayList<Sticker>();
	static File cardStorage = new File("card_storage.txt");
	static PrintWriter output = null;
	static Scanner input = null;
	static int lastEntryNumberAtStartup = 0;
    static int focusedLabelIndex = 0;
    static ImageView add_icon = new ImageView("buttons/add_button.jpg");
    static ImageView red_icon = new ImageView("buttons/red_button.jpg");
    static ImageView orange_icon= new ImageView("buttons/orange_button.jpg");
    static ImageView green_icon = new ImageView("buttons/green_button.jpg");
    static ImageView plain_icon = new ImageView("buttons/blank_button.jpg");
    static ImageView delete_icon = new ImageView("buttons/delete_button.jpg");
    static ImageView finish_icon = new ImageView("buttons/complete_button.jpg");
    static ImageView up_icon = new ImageView("buttons/up_arrow_button.jpg");
    static ImageView down_icon = new ImageView("buttons/down_arrow_button.jpg");
    static ImageView deadline_icon = new ImageView("buttons/deadline_button.jpg");
    static Screen activeScreen = Screen.getPrimary();
    static Rectangle2D screenBounds = activeScreen.getVisualBounds();
    static double screenWidth = screenBounds.getWidth();
    static double screenHeight = screenBounds.getHeight();

	public static void main(String[] args){	launch(args);	}
	
    @Override
	public void start(Stage myStage){
		
        myStage.setTitle("To Do Cards");
        myStage.setMaxHeight(screenHeight-10);
        myStage.setResizable(false);
        Scene myScene = new Scene(new RootNode());
        myScene.getStylesheets().add("styles.css");
		myStage.setScene(myScene);
            myStage.setOnCloseRequest(we -> {
                try{
                    backupStickers();
                    println("Stickers are successfully backed up. Over.");
                }
                catch(IOException e){println("Output error: "+ e);}
            });    
		myStage.show();
	}
	class RootNode extends HBox{
        RootNode(){
            stickersPanel = new VBox(10);
            ScrollPane scrollStickers = new ScrollPane(stickersPanel);
            scrollStickers.setPadding(new Insets(7,0,7,0));
            scrollStickers.setPrefViewportWidth(210);
            //scrollStickers.setFitToWidth(true);
            stickersPanel.setPadding(new Insets(5));
            controlPanel = new VBox(10);
            fillControlPanel();
            initCardStorage();
            postStickers();
            this.getChildren().addAll(scrollStickers, controlPanel);
            //this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                //stickersPanel.getChildren().get(focusedLabelIndex).
                //focusedLabelIndex = 0;
            //});
        }
	}
	
	public void fillControlPanel(){
		addNewButton = new MyButton(add_icon);
		addNewButton.addEventHandler(ActionEvent.ACTION, ae -> postNewSticker());
        //addNewButton.setAlignment(Pos.CENTER);
        //addNewButton.setGraphic(add_icon);
		risePriorityButton = new MyButton(up_icon);
        risePriorityButton.addEventHandler(ActionEvent.ACTION, ae ->{
            if(focusedLabelIndex>0) shiftUpper();
            else{
                ae.consume();
                getFocusedSticker().requestFocus();
            }
        });
		lowerPriorityButton = new MyButton(down_icon);
        lowerPriorityButton.addEventHandler(ActionEvent.ACTION, ae ->{
            if(focusedLabelIndex<getGUIlength()-1) shiftLower();
            else{
                ae.consume();
                getFocusedSticker().requestFocus();
            }
        });
		colorRedButton = new MyButton(red_icon);
        colorRedButton.setColorEventHandler("red", "tomato");
        colorOrangeButton = new MyButton(orange_icon);
        colorOrangeButton.setColorEventHandler("orange", "orange");
        colorGreenButton = new MyButton(green_icon);
        colorGreenButton.setColorEventHandler("green", "greenyellow");
        colorNeutralButton = new MyButton(plain_icon);
        colorNeutralButton.setColorEventHandler("neutral", "white");

        deleteButton = new MyButton(delete_icon);
        deleteButton.addEventHandler(ActionEvent.ACTION, ae -> deleteHandler());

        taskFinishButton = new MyButton(finish_icon);
        taskFinishButton.addEventHandler(ActionEvent.ACTION, ae -> {
            getFocusedSticker().setStatus("completed");
            deleteHandler();
        });
        
        deadlineButton = new MyButton(deadline_icon);
        deadlineButton.addEventHandler(ActionEvent.ACTION, ae -> {
            promptOnDeadline();
        });
        
        
        //                makeBoldButton.addEventHandler(ActionEvent.ACTION, ae ->{
//                    Sticker s = getFocusedSticker();
//                    if(s.getBoldState().equals("normal")){
//                            s.setFontWeight("bold");
//                    }
//                    else{
//                            s.setFontWeight("normal");
//                    }
//                    s.requestFocus();
//		});
        
		controlPanel.setPadding(new Insets(10, 20, 10, 20));
		controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setMaxWidth(75);
		controlPanel.getChildren().addAll(addNewButton, risePriorityButton, lowerPriorityButton, deadlineButton,
            colorRedButton, colorOrangeButton, colorGreenButton, 
            colorNeutralButton, taskFinishButton, deleteButton);
	}
    public void initCardStorage(){
        if(cardStorage.exists()){
            try{
                openInput();
                while(input.hasNextLine()){
                    readRecordInto(addNewSticker());
                }
                closeInput();
            }
            catch(Exception e) {println("Card storage creation error");}
        }
        else{
           try{
                addNewSticker();
                openOutput(true);
                closeOutput();
            }
            catch(Exception e) {println("Card storage creation error");}
        }     
    }
    public Sticker addNewSticker(){
        Sticker s = new Sticker();
        stickers.add(s);
        return s;
    }

    public static Sticker getFocusedSticker(){
        return (Sticker) stickersPanel.getChildren().get(focusedLabelIndex);
    }


    public void postStickers() {
        for(Sticker s: stickers){
            //uncomment to filter the completed tasks away from being posted on the panel
            if(!s.getStatus().equals("completed"))
                stickersPanel.getChildren().add(s);
            switch(s.getColor()){
                case "neutral": break;
                case "red": s.paint("tomato"); break;
                case "orange": s.paint("orange"); break;
                case "green": s.paint("greenyellow"); break;
            }
            if(getGUIlength()<1){
                postNewSticker();
                break;
            }
            
            //if(s.getBoldState().equals("bold"))  s.setFontWeight("bold");
            //printToConsole(s);
        }
    }
    public void postNewSticker() {
        Sticker s = addNewSticker();
        stickersPanel.getChildren().add(focusedLabelIndex, s);
        s.requestFocus();
    }
        
    public void shiftUpper(){
        shift(-1);
    }

    public void shiftLower(){
        shift(1);
    }
    
    public void shift(int direction){
        Sticker node = getFocusedSticker();
        stickersPanel.getChildren().remove(focusedLabelIndex);
        stickersPanel.getChildren().add(focusedLabelIndex+direction, node);
        node.requestFocus();
        focusedLabelIndex = getGUIposition(node);
    }

    public static int getGUIposition(Sticker s){
        return stickersPanel.getChildren().indexOf(s);
    }
    public int getGUIlength(){
        return stickersPanel.getChildren().size();
    }

	public void readRecordInto(Sticker s){
		String recordLine = input.nextLine();
		String[] recordItems = recordLine.split(";;");
		for(int i=0; i<recordItems.length; i++){
			switch(i){
				case 0: {
					int number = Integer.parseInt(recordItems[i]);
					s.setEntryNumeral(number);
					if(number>lastEntryNumberAtStartup) lastEntryNumberAtStartup=number;
					break;
				}
				case 1: s.setPosition(Integer.parseInt(recordItems[i])); break;
				case 2: s.setColor(recordItems[i]); break;
				case 3: s.setStatus(recordItems[i]); break;
				case 4: {
                    String[] inputArray = recordItems[i].split("<br>");
                    String content = inputArray[0];
                    for(int a = 1; a<inputArray.length; a++){
                        content+="\n"+inputArray[a];
                    }
                    s.setText(content);
                    break;
                } 
				case 5: s.setDeadline(recordItems[i]); break;
                    case 6: s.setBoldState(recordItems[i]); break;
			}
		}
		s.tooltipStaticPart = "Entry " + s.getEntryNumeral();
		if(s.getStatus().equals("incomplete"))
			s.tooltipDynamicPart = "incomplete: to do by "+ s.getDeadline();
		else s.tooltipDynamicPart = "surveyed";
		s.updateTooltip();
	}
	
	public void makeRecord(Sticker s) throws IOException{
		output.print(s.getEntryNumeral()+ ";;");
		output.print(s.getPosition()+ ";;");
		output.print(s.getColor()+ ";;");
		output.print(s.getStatus()+ ";;");
        String content = s.getText();
        input = new Scanner(content);
        String outputString = "";
        if(input.hasNextLine())
            outputString += input.nextLine();
        while(input.hasNextLine()){
            outputString += "<br>" + input.nextLine();   
        }
		output.print(outputString+ ";;");
		output.print(s.getDeadline()+ ";;");
        output.println(s.getBoldState());
	}
        
    public void backupStickers() throws IOException{
        openOutput(false);
        output.print("");
        closeOutput();
        openOutput(true);
        for(Node s : stickersPanel.getChildren()){
            Sticker concerned = (Sticker) s;
            if(concerned.getText().equals(""));
            else makeRecord(concerned);
        }
        for(Sticker s : stickers){
            if(s.getStatus().equals("completed"))
                makeRecord(s);
        }
        closeOutput();
        Files.copy(cardStorage.toPath(), new File(defineBackupStorageName()).toPath());
    }
       
    public String defineBackupStorageName(){
        Date date = new Date();
        SimpleDateFormat formatDate = new SimpleDateFormat("yy_MM_dd_hh.mm");
        String dateString = formatDate.format(date);
        return "backupstorage_"+ dateString+".txt";
    }
	
	public void openOutput(boolean isAppendable) throws IOException{
		output = new PrintWriter(new FileWriter(cardStorage, isAppendable));
	}
	public void closeOutput() throws IOException{
		output.close();
	}
	public void openInput() throws IOException{
		input = new Scanner(new FileInputStream(cardStorage));
	}
	public void closeInput() throws IOException{
		input.reset();
                input.close();
	}
	
    public void promptOnDeadline(){
        new DialogInput();
    }
    
    void deleteHandler(){
        if(getGUIlength()>1){
            stickersPanel.getChildren().remove(focusedLabelIndex);
            if(focusedLabelIndex>0) focusedLabelIndex--;
            getFocusedSticker().requestFocus();
        }
        else{
            postNewSticker();
            stickersPanel.getChildren().remove(1);
        }
    }
                
	public void printToConsole(Sticker s){
		print(s.getEntryNumeral() + "\t");
		print(s.getPosition() + "\t");
		print(s.getColor() + "\t");
		print(s.getStatus() + "\t");
		print(s.getText() + "\t");
		println(s.getDeadline());
	}
	
	static void print(Object object) {System.out.print(object);}
	static void println(Object object) {System.out.println(object);}
        
    public class MyButton extends Button{
        MyButton(ImageView image){
            this.setAlignment(Pos.CENTER);
            this.setGraphic(image);
            image.setFitHeight(40.0);
            image.setPreserveRatio(true);
        }

        void setColorEventHandler(String colorTitle, String styleSetting){
            this.addEventHandler(ActionEvent.ACTION, ae ->{
                Sticker s = getFocusedSticker();
                s.paint(styleSetting);
                s.setColor(colorTitle);
                s.requestFocus();
            });
        }
    }
}