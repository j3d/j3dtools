/*****************************************************************************
 *                        J3D.org Copyright (c) 2000
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.ui.navigation;

// Standard imports
import  javax.swing.*;
import  java.text.*;
import  java.net.*;
import  java.util.*;
import  java.awt.*;
import  java.awt.event.*;
import  javax.media.j3d.*;
import  javax.vecmath.*;

/**
    This is the toolbar for all navigation and view manipulation commands.
    @author <a href="http://www.geocities.com/seregi/index.html">Laszlo Seregi</a><br>
 */
public class NavigationToolbar extends JPanel
    implements NavigationStateListener
{
    /** The current navigation state either set from us or externally */
    private int navigationState = WALK_STATE;

    /** An observer for navigation state change information */
    private NavigationStateListener navigationListener;

/*
    private     Ddd                         ddd;
    private     JComboBox                   jComboBoxViews          =   new JComboBox       ();
    public      ButtonGroup                 buttonGroupNavigation   =   new ButtonGroup();
    public      ButtonGroup                 buttonGroupMode         =   new ButtonGroup();
    public      JToggleButton               buttonWalk              =   new JToggleButton   (new ImageIcon(ClassLoader.getSystemResource("images/ButtonWalk.gif")));
    public      JToggleButton               buttonTilt              =   new JToggleButton   (new ImageIcon(ClassLoader.getSystemResource("images/ButtonTilt.gif")));
    public      JToggleButton               buttonPan               =   new JToggleButton   (new ImageIcon(ClassLoader.getSystemResource("images/ButtonPan.gif")));
    public      JButton                     buttonViewForward       =   new JButton         (new ImageIcon(ClassLoader.getSystemResource("images/ButtonForward.gif")));
    public      JButton                     buttonViewBackward      =   new JButton         (new ImageIcon(ClassLoader.getSystemResource("images/ButtonBack.gif")));
    public      JButton                     buttonStraighten        =   new JButton         (new ImageIcon(ClassLoader.getSystemResource("images/ButtonHome.gif")));
    public      JPopupMenu                  popup                   =   new JPopupMenu      ("Navigation3D");
    public      Cursor                      cursorSelection         =   null;
    private     Canvas3D                    dddCanvas3D;
    private     View                        view;
    private     TransformGroup              tgView;
    //===============================================================

    public DddToolBar(Ddd ddd,Canvas3D dddCanvas3D,View view,TransformGroup tgView)
    {
        this.ddd                =   ddd;
        this.dddCanvas3D        =   dddCanvas3D;
        this.view               =   view;
        this.tgView             =   tgView;
        setFloatable(false);
        // buttonGroupNavigation:
        buttonGroupNavigation.add(buttonWalk);
        buttonGroupNavigation.add(buttonTilt);
        buttonGroupNavigation.add(buttonPan);
        // setMargin:
        buttonWalk.setMargin            (new Insets(0,0,0,0));
        buttonTilt.setMargin            (new Insets(0,0,0,0));
        buttonPan.setMargin            (new Insets(0,0,0,0));
        buttonViewForward.setMargin     (new Insets(0,0,0,0));
        buttonViewBackward.setMargin    (new Insets(0,0,0,0));
        buttonStraighten.setMargin      (new Insets(0,0,0,0));
        jComboBoxViews.setMinimumSize   (new Dimension(60,10));
        // addActionListener:
        buttonWalk          .addActionListener(new actionListener());
        buttonTilt          .addActionListener(new actionListener());
        buttonPan          .addActionListener(new actionListener());
        buttonViewForward   .addActionListener(new actionListener());
        buttonViewBackward  .addActionListener(new actionListener());
        buttonStraighten    .addActionListener(new actionListener());
        jComboBoxViews      .addActionListener(new actionListener());
        //  setToolTipText:
        buttonWalk          .setToolTipText("Walk");
        buttonTilt          .setToolTipText("Tilt");
        buttonPan           .setToolTipText("Pan");
        buttonViewForward   .setToolTipText("Next Viewpoint");
        buttonViewBackward  .setToolTipText("Previous Viewpoint");
        buttonStraighten    .setToolTipText("Straighten");
        jComboBoxViews      .setToolTipText("Views");
        //  add the Components to the toolbar:
        add(buttonViewBackward);
        add(jComboBoxViews);
        add(buttonViewForward);
        addSeparator();
        add(buttonWalk);
        add(buttonTilt);
        add(buttonPan);
        addSeparator();
        add(buttonStraighten);
        addSeparator();
    }
    //===============================================================================================================
    public void initialize(BranchGroup bgArena,Scene[] sceneArenas)
    {
        if(bgArena!=null && sceneArenas!=null)
        {
            TransformGroup[]    tgLandmark_;
            Transform3D         t3dLandmark     =   new Transform3D();
            //  Views:
            jComboBoxViews.removeAllItems();
            for(int t=0;t<sceneArenas.length;t++)
            {
                tgLandmark_=sceneArenas[t].getViewGroups();
                if(tgLandmark_==null)break;
                for(int n=0;n<tgLandmark_.length;n++)
                {
                    jComboBoxViews.addItem(new UserData("Landmark",n,tgLandmark_[n]));
                    // Select the first landmark to start with:
                    if(n==0)
                    {
                        tgLandmark_[n].getTransform(t3dLandmark);
                        tgView.setTransform(t3dLandmark);
                    }
                }
            }
        }
        jComboBoxViews.addItem(new UserData("Goto ",0,new TransformGroup()));
        //  Actions:
        buttonWalk.doClick();
    }
    //==========================================================================================================
    // This Class catches all the toolbar events.
    private class actionListener implements ActionListener
    {
        Object  o;
        int     selection0=-1;
        public void actionPerformed(ActionEvent e)
        {
            o=e.getSource();
            //  Radio buttons:
            if(buttonWalk.isSelected()) cursorSelection=ddd.dddMouseKeyboardInput.cursorWalk;
            if(buttonTilt.isSelected()) cursorSelection=ddd.dddMouseKeyboardInput.cursorTilt;
            if(buttonPan.isSelected()) cursorSelection=ddd.dddMouseKeyboardInput.cursorPan;
            ddd.dddMouseKeyboardInput.setCursor(cursorSelection);
            //  View controls:
            if(o==buttonViewBackward)   {setViewSelection(-1,true);}
            if(o==buttonViewForward)    {setViewSelection(+1,true);}
            if(o==jComboBoxViews)       {setViewSelection(((JComboBox)o).getSelectedIndex(),false);}
            //  buttonStraighten:
            if(o==buttonStraighten)
            {
                Transform3D t3d             =   new Transform3D();
                Point3d     direction       =   new Point3d(0,0,-1);
                Vector3d    vUp             =   new Vector3d(0,1,0);
                Vector3d    v3d     =   new Vector3d();
                tgView.getTransform(t3d);
                t3d.get(v3d);
                Point3d     eye     =   new Point3d(v3d);
                tgView.getTransform(t3d);
                t3d.transform(direction);
                direction.y=eye.y;
                t3d.lookAt(eye,direction,vUp);
                t3d.invert();
                ddd.dddMouseKeyboardInput.transitionView.transitionTo(tgView,t3d,1000);
            }
        }
        //======================================================================================================
        public void setViewSelection(int i,boolean relative)
        {
            int selection=jComboBoxViews.getSelectedIndex();
            if(selection==-1)return;
            int lastItem=jComboBoxViews.getItemCount()-1;
            if(relative)selection=selection+i;
            else        selection=i;
            if(selection>lastItem)selection=0;
            if(selection<0)selection=lastItem;
            jComboBoxViews.setSelectedIndex(selection);
            if(relative)return;// return because otherwise it will be called twice
            selection0=selection;
            //  Set the viewpoint:
            Transform3D t3d=new Transform3D();
            //  ComboBox
            //System.out.println("selection"+selection);
            UserData userObject=(UserData)jComboBoxViews.getSelectedItem();
            userObject.tg.getTransform(t3d);
            ddd.dddMouseKeyboardInput.transitionView.transitionTo(tgView,t3d,1000);
        }
    }
*/

    /**
     * Set the listener for navigation state change notifications. By setting
     * a value of null it will clear the currently set instance
     *
     * @param l The listener to use for change updates
     */
    public void setNavigationStateListener(NavigationStateListener l)
    {
        navigationListener = l;
    }

    //----------------------------------------------------------
    // Methods required by the NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the panning state has changed to the new state.
     *
     * @param state One of the state values declared here
     */
    public void setNavigationState(int state)
    {
        navigationState = state;

        switch(navigationState)
        {
            case NavigationStateListener.WALK_STATE:
                break;

            case NavigationStateListener.PAN_STATE:
                break;

            case NavigationStateListener.TILT_STATE:
                break;
        }
    }

    /**
     * Callback to ask the listener what navigation state it thinks it is
     * in.
     *
     * @return The state that the listener thinks it is in
     */
    public int getNavigationState()
    {
        return navigationState;
    }
}
