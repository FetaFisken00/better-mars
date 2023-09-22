package mars.venus;
   import mars.simulator.*;
	import mars.*;
   import java.util.*;
   import java.awt.*;
   import java.awt.event.*;
   import javax.swing.*;
   import java.io.*;

import mars.Globals;

public class SettingsToggleDarkmodeAction extends GuiAction {
    public SettingsToggleDarkmodeAction(String name, Icon icon, String descrip,
                             Integer mnemonic, KeyStroke accel, VenusUI gui) {
         super(name, icon, descrip, mnemonic, accel, gui);
      }

      public void actionPerformed(ActionEvent e) {
        Globals.getSettings().setStartAtMain(((JCheckBoxMenuItem)e.getSource()).isSelected());
   }

}
