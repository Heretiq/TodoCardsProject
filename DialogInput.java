import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;
import java.text.*;

public class DialogInput {
    Stage promptStage;
    TextField textField;
    Label promptLabel;
    VBox promptControls;
    Button submitButton;
    int checkCounter = 0;
    
    DialogInput(){
        promptStage = new Stage();
        promptStage.setTitle("Deadline input");
        promptControls = new VBox();
        promptControls.setMinWidth(300);
        promptControls.setPadding(new Insets(5));
        promptLabel = new Label("Type the deadline here: DDMMYY");
        promptLabel.setMinHeight(40.0);
        textField = new TextField();
        textField.setMaxWidth(100);
        textField.setOnAction(ae -> TodoCards.getFocusedSticker().setDeadline(checkNsubmit()));
        submitButton = new Button("Submit");
        submitButton.setOnAction(ae -> TodoCards.getFocusedSticker().setDeadline(checkNsubmit()));
        promptControls.getChildren().addAll(promptLabel, textField, submitButton);
        Scene scene = new Scene(promptControls);
        promptStage.setScene(scene);
        promptStage.showAndWait();
    }
    
    public String checkNsubmit(){
        textField.requestFocus();
        GregorianCalendar today = new GregorianCalendar();
        String input = textField.getText();
        if(input.length() != 6){
            reportError("Try again following the DDMMYY template and submit");
            return "no deadline";
        }
        int day, month, year;
        String dayLine, monthLine, yearLine;
        dayLine = input.substring(0,2);
        monthLine = input.substring(2,4);
        yearLine = input.substring(4);
        if(input.charAt(0) == '0') day = Character.getNumericValue(input.charAt(1));
        else day = Integer.parseInt(dayLine);
        if(input.charAt(2) == '0') month = Character.getNumericValue(input.charAt(3));
        else month = Integer.parseInt(monthLine);
        if(input.charAt(4) == '0') year = Character.getNumericValue(input.charAt(5))+2000;
        else year = Integer.parseInt(yearLine)+2000;
        if(month>12){
            reportError("Try again following the DDMMYY template and submit");
            return "no deadline";
        }
        if(day>31 && (month==1 || month==3 || month==5 ||month==7 || month==8 ||month==10 || month==12)){
            reportError("Try again following the DDMMYY template and submit");
            return null;
        }
        if(day>30 && (month==4 || month==6 ||month==9 || month==11)){
            reportError("Try again following the DDMMYY template and submit");
            return "no deadline";
        }
        if(day>29 && month==2 && today.isLeapYear(year)){
            reportError("Try again following the DDMMYY template and submit");
            return "no deadline";
        }
        if(day>28 && month==2 && today.isLeapYear(year)==false){
            reportError("Try again following the DDMMYY template and submit");
            return "no deadline";
        }
        GregorianCalendar deadlineDate = new GregorianCalendar(year, month-1, day);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH)+1;
        int todayYear = today.get(Calendar.YEAR);
        if(today.after(deadlineDate)){
            reportError("The deadline cannot be earlier than the current date.\nTry again following the DDMMYY template and submit");
            return "no deadline";
        }
        if(year>todayYear && checkCounter==0){
            reportError("Are you sure this will be next year?\nTry again following the DDMMYY template and submit");
            checkCounter++;
            return "no deadline";
        }
        //String date = dayLine +"."+ monthLine +".20"+ yearLine;
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        String date = formatter.format(deadlineDate.getTime());
        System.out.println("Deadline is set on "+ date);
        promptStage.close();
        checkCounter=0;
        if(date!=null) return date;
        else return "no deadline";
    }
    
    public void reportError(String errorDescription){
        textField.setText("");
        promptLabel.setStyle("-fx-text-fill: tomato;");
        promptLabel.setText(errorDescription);
    }
}
