package mars.tools;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.*;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

import mars.tools.TrafficLight.LightColor;
import mars.util.SystemIO;
import mars.Globals;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Memory;
import mars.mips.hardware.MemoryAccessNotice;
import mars.simulator.Exceptions;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
@SuppressWarnings("serial")
/* Add these two lines in exceptions.java file
 * public static final int EXTERNAL_INTERRUPT_TIMER = 0x00000100; //Add for digital Lab Sim
 * public static final int EXTERNAL_INTERRUPT_HEXA_KEYBOARD = 0x00000200;// Add for digital Lab Sim
*/


public class TraficLightTool extends AbstractMarsToolAndApplication {
	private static String heading =  "Traffic Light Simulator";
	private static String version = " Version 1.0 (Hans Hedbom)";
	private static final int IN_ADRESS_TRAFFIC_1=Memory.memoryMapBaseAddress+0x10;
	private static final int IN_ADRESS_TRAFFIC_2=Memory.memoryMapBaseAddress+0x11;
	private static final int IN_ADRESS_COUNTER=Memory.memoryMapBaseAddress+0x12;
	private static final int OUT_ADRESS_BUTTON=Memory.memoryMapBaseAddress+0x13;
	private TrafficLight   light1;
    private TrafficLight   light2;


  public static final int EXTERNAL_INTERRUPT_TIMER = 0x00000100; //Add for digital Lab Sim
  public static final int EXTERNAL_INTERRUPT_BUTTON = 0x0000200;// Add for digital Lab Sim

	// GUI Interface. 
   	private static JPanel panelTools;
   	private  JPanel buttonPanel;
   	// Seven Segment display
	private JFXPanel trafficPanel;
	
	
	private static int CounterValueMax=30; 
	private static int CounterValue=CounterValueMax;
	private static boolean CounterInterruptOnOff=false;
	private static OneSecondCounter SecondCounter;

	public TraficLightTool(String title, String heading) {
		super(title,heading);
	}
	public TraficLightTool() {
		super (heading+", "+version, heading);
	}
	public static void main(String[] args) {
		new TraficLightTool(heading+", "+version,heading).go();
	}
	public String getName() {
		return "Traffic Light Sim";
	}
	protected void addAsObserver(){
    	addAsObserver(IN_ADRESS_TRAFFIC_1, IN_ADRESS_TRAFFIC_1);
    	addAsObserver(Memory.textBaseAddress, Memory.textLimitAddress);
    }
	public void update(Observable ressource, Object accessNotice){
    	MemoryAccessNotice notice = (MemoryAccessNotice) accessNotice;
    	int address=notice.getAddress();
    	int value= notice.getValue();
    	if(address == IN_ADRESS_TRAFFIC_1)
    			updateTraffic(1, value);
    	else
    		if  (address == IN_ADRESS_TRAFFIC_2)
    			updateTraffic(2, value);
    		/*else
				if (address == OUT_ADRESS_BUTTON)
					resetButton();*/
    		
				else
    				if (address == IN_ADRESS_COUNTER)
    					updateOneSecondCounter(value);
    	if  (CounterInterruptOnOff)
    		if (CounterValue >0){
    			CounterValue--;
    		}
    		else{
    			CounterValue=CounterValueMax;
    			if((Coprocessor0.getValue(Coprocessor0.STATUS) & 2)==0  // Added by Carl Hauser Nov 2008
		                  && (Coprocessor0.getValue(Coprocessor0.STATUS) & 1)==1){
    				mars.simulator.Simulator.externalInterruptingDevice = EXTERNAL_INTERRUPT_TIMER;
    			}
    		}
	}
	
	private void updateTraffic(int i, int value) {
		if(i==1){
			if(value==0){
				light1.setUpperOn(false);
                light1.setMiddleOn(false);
                light1.setLowerOn(false);
			}
			if(value==1){
				light1.setUpperOn(true);
                light1.setMiddleOn(true);
                light1.setLowerOn(false);
			}
			if(value==2){
				light1.setUpperOn(false);
                light1.setMiddleOn(false);
                light1.setLowerOn(true);
			}
		
			if(value==3){
				light1.setUpperOn(true);
				light1.setMiddleOn(true);
				light1.setLowerOn(true);
			}
		}
		if(i==2){
			if(value==0){
				light2.setUpperOn(false);
                light2.setMiddleOn(false);
                light2.setLowerOn(false);
			}
			if(value==1){
				light2.setUpperOn(true);
                light2.setMiddleOn(false);
                light2.setLowerOn(false);
			}
			if(value==2){
				light2.setUpperOn(false);
                light2.setMiddleOn(true);
                light2.setLowerOn(false);
			}
			if(value==3){
				light2.setUpperOn(true);
                light2.setMiddleOn(true);
                light2.setLowerOn(false);
			}
			if(value==4){
				light2.setUpperOn(false);
                light2.setMiddleOn(false);
                light2.setLowerOn(true);
			}
			if(value==6){
				light2.setUpperOn(false);
                light2.setMiddleOn(true);
                light2.setLowerOn(true);
			}
		}
		
	}
	protected void reset(){
    	
    	SecondCounter.resetOneSecondCounter();
    	updateTraffic(1,1);
    	updateTraffic(2,4);
    }
    protected JComponent buildMainDisplayArea() {
    	System.out.println("Bulding Dispaly Start");
    	panelTools=new JPanel(new GridLayout(1,2));
    	System.out.println("Bulding Lights Start");
    	buildLight();
    	System.out.println("Bulding Lights End");
    	System.out.println("Bulding Buttons Start");
    	buildButtonPannel();
    	System.out.println("Bulding Buttons End");
	 	panelTools.add(trafficPanel);
	 	
	 	panelTools.add(buttonPanel);
	 	SecondCounter= new OneSecondCounter();
	   	return panelTools;
    }
    
    protected void buildLight() {
    	trafficPanel=new JFXPanel();
    	light1 = TrafficLightBuilder.create()
                .prefSize(140, 480)
                .upperOn(true)
                .upperOverlayVisible(true)
                .middleOn(true)
                .middleColor(LightColor.RED)
                .middleOverlayVisible(true)
                .lowerOn(false)
                .lowerOverlayVisible(true)
                .build();
    	light2 = TrafficLightBuilder.create()
                .prefSize(140, 480)
                .backgroundColor(Color.DARKORANGE)
                .upperOn(false)
                .middleOn(false)
                .lowerOn(true)
                .build();
    	HBox box = new HBox(light1, light2);
        box.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        box.setSpacing(10);
        box.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(box);
        trafficPanel.setScene(scene);
        trafficPanel.setPreferredSize(new Dimension(140,280));
    }
    
    protected void buildButtonPannel(){
    	JButton walk = new JButton();
    	  try {
    	    Image img = ImageIO.read(getClass().getResource(Globals.imagesPath+"prisma.jpg"));
    	    walk.setIcon(new ImageIcon(img));
    	  } catch (Exception ex) {
    	    System.out.println(ex);
    	  }

        walk.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				if ((Coprocessor0.getValue(Coprocessor0.STATUS) & 2)==0  // Added by Carl Hauser Nov 2008
        		                  && (Coprocessor0.getValue(Coprocessor0.STATUS) & 1)==1) {
        					      updateMMIOControlAndData(OUT_ADRESS_BUTTON, 1);
        		                  mars.simulator.Simulator.externalInterruptingDevice = EXTERNAL_INTERRUPT_BUTTON;
        			}
        			}	
        		});		
        JButton drive = new JButton();
        try {
    	    Image img = ImageIO.read(getClass().getResource(Globals.imagesPath+"detector.jpg"));
    	    drive.setIcon(new ImageIcon(img));
    	  } catch (Exception ex) {
    	    System.out.println(ex);
    	  }
        drive.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				if ((Coprocessor0.getValue(Coprocessor0.STATUS) & 2)==0  // Added by Carl Hauser Nov 2008
     		                   && (Coprocessor0.getValue(Coprocessor0.STATUS) & 1)==1) {
     					      updateMMIOControlAndData(OUT_ADRESS_BUTTON, 2);
     		                  mars.simulator.Simulator.externalInterruptingDevice = EXTERNAL_INTERRUPT_BUTTON;
     			}
        			}
        		});
       JTextField textField = new JTextField(5);
       textField.setText(String.valueOf(CounterValueMax));
       JButton set = new JButton("Set Timer");
       set.addActionListener(
       		new ActionListener() {
       			public void actionPerformed(ActionEvent e) {
       			int value = Integer.parseInt(textField.getText());
       			CounterValueMax=value;
    			}
       			}
       		);
       JPanel textPanel = new JPanel(new BorderLayout());
       textPanel.add(set,BorderLayout.LINE_START);
       textPanel.add(textField,BorderLayout.CENTER);
       buttonPanel=new JPanel(new BorderLayout());
       buttonPanel.add(walk,BorderLayout.LINE_START);
       buttonPanel.add(drive,BorderLayout.LINE_END);
       buttonPanel.add(textPanel,BorderLayout.SOUTH);
    	
    }
    private synchronized void updateMMIOControlAndData(int dataAddr, int dataValue) {
        if (!this.isBeingUsedAsAMarsTool || (this.isBeingUsedAsAMarsTool && connectButton.isConnected())) {
           synchronized (Globals.memoryAndRegistersLock) {
              try {
              		Globals.memory.setByte(dataAddr, dataValue);
              } 
                  catch (AddressErrorException aee) {
                    System.out.println("Tool author specified incorrect MMIO address!"+aee);
                    System.exit(0);
                 }
           }
           if (Globals.getGui() != null && Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().getCodeHighlighting() ) {
              Globals.getGui().getMainPane().getExecutePane().getDataSegmentWindow().updateValues();
           }
        }
     }
    protected JComponent getHelpComponent() {
    	 final String helpContent = 
    	        	" Instructions for using the traffic light tool.\n"+
    	        			"\n"+
"The traffic light tool provides two traffic lights. One for car traffic and one for pedestrians.\n"+
"The colour or mode of these lights are controlled through memory mapped io where the Byte value at address 0xFFFF0010 : controls the traffic light for the pedestrians.\n"+
"Dark=0x00\n"+
"Stop=0x01 (bit 0)\n"+
"Walk=0x02 (bit 1)\n"+
"\n"+
"Byte value at address 0xFFFF0011 : controls the normal traffic light with:\n"+
"Dark = 0x00\n"+ 
"Red=0x01 (bit0)\n"+
"Orange=0x02 (bit1)\n"+
"Green=0x04 (bit2)\n"+
"\n"+
"In order to light one of the colours in the traffic light the values above is written to the memory address e.g. if we want to have a red light the following code is used:\n"+
"la      $t0, 0xFFFF0011   	#Set red light om main road\n"+
"add   $t1,$zero, 0x01\n"+
"sb      $t1, 0x0($t0)\n"+ 
"\n"+
"The tool also has two push buttons these generates interrupts on external interrupt 1 (ie. Bit 11 in the status register) when pushed. Which button that has been pushed is stored in the memory byte at address 0xFFFF0013 where the walk button =0x01 (bit 0) and the drive button = 0x02 (bit 0).  The value is overwritten the next time a button is pressed.\n"+
"\n"+
"The tool also contains a timer. The timer is enabled by writing any value (>0) to address 0xFFFF0012.  If timer interruption is enable, every 30 instructions, the timer generates interrupts on external interrupt 0 (ie. Bit 10 in the Status register) this can be used as one time unit for labs etc.\n"+
"\n"+
"(PLEASE DO NOT USE THE TIMER WHEN MARS IS RUNNING AT FULL SPEED ALWAYS HAVE THE SPEED SET TO <30 inst/Sec.)\n";
        JButton help = new JButton("Help");
        help.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				JTextArea ja = new JTextArea(helpContent);
        				ja.setRows(20);
        				ja.setColumns(60);
        				ja.setLineWrap(true);
        				ja.setWrapStyleWord(true);
        				JOptionPane.showMessageDialog(theWindow, new JScrollPane(ja),
                        "Simulating a traffic light with sensors and walkway button", JOptionPane.INFORMATION_MESSAGE);
        			}
        		});		
        return help;  
    }
   
   
 
/* ....................Timer start here................................... */
    public void updateOneSecondCounter(int value) {
    	if (value !=0){
    		CounterInterruptOnOff=true;
    		CounterValue=CounterValueMax;
    	}
    	else{
    		CounterInterruptOnOff=false;
    	}
    }
    public class OneSecondCounter{
    	public OneSecondCounter(){
    		CounterInterruptOnOff=false;
    	}
 	    public void resetOneSecondCounter(){
	    	CounterInterruptOnOff=false;
	    	CounterValue=CounterValueMax;
	    }
    }
}
