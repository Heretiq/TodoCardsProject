import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Callable;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.beans.binding.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.text.Font;

public class Sticker extends TextArea{
	private int entryNumeral=0;
	private int position=0; 
	private String color = "neutral"; //"red", "orange", "green", "neutral"
	private String status = "incomplete"; // "incomplete", "completed", "surveyed"
	private String deadline = "no deadline";
    private String boldState = "normal";
    private double stickerHeight = 80.0;
    private double stickerWidth = 200.0;
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	Tooltip newTooltip = new Tooltip();
	String tooltipStaticPart, tooltipDynamicPart;
    
	Sticker(){
		TextArea thisSticker = this;
       thisSticker.setFont(Font.font(null, 13));
        thisSticker.setPrefWidth(stickerWidth);
        thisSticker.setPrefHeight(stickerHeight);
		thisSticker.setMinHeight(stickerHeight);
        thisSticker.setWrapText(true);
        thisSticker.sceneProperty().addListener(new ChangeListener<Scene>(){
            @Override
            public void changed(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene){
                if(newScene!=null){
                    thisSticker.applyCss();
                    Node text = thisSticker.lookup(".text");
                    //public static DoubleBinding createDoubleBinding(Callable<Double> func, Observable... dependencies)
                    thisSticker.prefHeightProperty().bind(Bindings.createDoubleBinding(new Callable<Double>(){
                        @Override
                        public Double call(){
                            return text.getBoundsInLocal().getHeight()+8;
                        }
                    }, text.boundsInLocalProperty()));
                }
            }
        });
        
		Tooltip.install(this, newTooltip);
        this.newTooltip.setFont(Font.font(null, 13));
        //this.newTooltip.
        TodoCards.lastEntryNumberAtStartup = ++TodoCards.lastEntryNumberAtStartup;
        this.setEntryNumeral(TodoCards.lastEntryNumberAtStartup);
		tooltipStaticPart = "Entry " + TodoCards.lastEntryNumberAtStartup;
		tooltipDynamicPart = this.getStatus() +": to do by " +this.getDeadline();
		updateTooltip();
		this.pcs.addPropertyChangeListener(new TooltipListener(this));
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            TodoCards.focusedLabelIndex = TodoCards.getGUIposition(this);
            println("focusedLabelIndex ="+  TodoCards.focusedLabelIndex);
        }); 
	}
	
	class TooltipListener implements PropertyChangeListener{
		Sticker aSticker;
		TooltipListener(Sticker newSticker){
			this.aSticker = newSticker;
		}
		public void propertyChange(PropertyChangeEvent event){
			if("status".equals(event.getPropertyName())){
				if(event.getNewValue().equals("incomplete"))
					aSticker.tooltipDynamicPart = "incomplete: to do by "+ aSticker.getDeadline();
				else if(event.getNewValue().equals("surveyed"))
					aSticker.tooltipDynamicPart = "surveyed";
			}
			else if("deadline".equals(event.getPropertyName())){
				aSticker.tooltipDynamicPart = "incomplete: to do by "+ event.getNewValue();
			}
			aSticker.updateTooltip();
		}
	}
    
	int getEntryNumeral() {
		return entryNumeral;
	}

	void setEntryNumeral(int entryNumeral) {
		this.entryNumeral = entryNumeral;
	}

	int getPosition() {
		return TodoCards.getGUIposition(this);
	}

	void setPosition(int newPosition) {
		this.position = newPosition;
	}
        
    String getColor() {
		return color;
	}

	void setColor(String color) {
		this.color = color;
	}

	String getStatus() {
		return status;
	}

	void setStatus(String newStatus) {
		String oldStatus = this.status;
		this.status = newStatus;
		pcs.firePropertyChange("status", oldStatus, newStatus);
	}
    String getBoldState() {
		return boldState;
	}

	void setBoldState(String newStatus) {// "bold" or "normal"
		this.boldState = newStatus;
	}
    void setFontWeight(String newStatus) {// "bold" or "normal"
        this.setStyle("-fx-font-weight: "+ newStatus);
        this.setBoldState(newStatus);
	}
        
	String getDeadline() {
		return deadline;
	}

	void setDeadline(String newDeadline) {
		String oldDeadline = this.deadline;
		this.deadline = newDeadline;
		pcs.firePropertyChange("deadline", oldDeadline, newDeadline);	
	}
    
    void updateTooltip(){
		this.newTooltip.setText(tooltipStaticPart+ "\n"+tooltipDynamicPart);
	}
    
    void paint(String styleSetting){
        this.setStyle("text-area-background: " + styleSetting + ";");
    }
    
	static void print(Object object) {System.out.print(object);}
	static void println(Object object) {System.out.println(object);}
}