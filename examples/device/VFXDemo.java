/*****************************************************************************
 *                      Modified version (c) j3d.org 2002
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

import org.j3d.device.input.vfx.*;

/**
 * Demo for The VFX 3D HMD setup.
 */
public class VFXDemo extends JFrame
{
/*
    private JPanel itsGraphicsPanel = new JPanel();
    BorderLayout itsFrameLayout = new BorderLayout();
    BorderLayout itsGraphicsPanelLayout = new BorderLayout();

    private Canvas3D  itsCanvas3D = null;
    private final InputDeviceBase itsInputDevice;

    private final Locale       itsLocale = new Locale( new VirtualUniverse( ) );
    private final BranchGroup  itsInitialBranchGroup = new BranchGroup( );

    private final TransformGroup  itsCameraTG = new TransformGroup( );
    private final TransformGroup  itsObjectTG = new TransformGroup( );

*/

    /**
     * Construct a new instance of the demo.
     */
    public VFXDemo()
    {
        super("VFX HMD demo");

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        template.setDoubleBuffer(GraphicsConfigTemplate3D.REQUIRED);
        template.setStereo(GraphicsConfigTemplate3D.REQUIRED);

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev = env.getDefaultScreenDevice();

        GraphicsConfiguration gfx_config =
            dev.getBestConfiguration(template);

        Container content_pane = getContentPane();

        Canvas3D canvas = new Canvas3D(gfx_config);
        content_pane.add(canvas);
    }

/*

    private void buildSceneGraph( )
    {
        final Shape3D itsTestObject = new TestShape3d( );
        itsObjectTG.addChild( itsTestObject );
        itsObjectTG.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
        itsObjectTG.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );

        final ViewPlatform platform = new ViewPlatform( );
        platform.setCapability( TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ );
        final Transform3D initialCameraTransform = new Transform3D( );
        // view the scene from right above
        initialCameraTransform.lookAt
        (
            new Point3d( 5.0, 5.0, 5.0 ),
            new Point3d( ),
            new Vector3d( 0.0, 0.0, 1.0 )
        );
        initialCameraTransform.invert( );
        itsCameraTG.setTransform( initialCameraTransform );
        itsCameraTG.addChild( platform );
        itsCameraTG.setCapability( TransformGroup.ALLOW_TRANSFORM_READ );
        itsCameraTG.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );

        final View camera = new View( );
        camera.setPhysicalEnvironment( new PhysicalEnvironment( ) );
        camera.setPhysicalBody( new PhysicalBody( ) );
        camera.attachViewPlatform( platform );
        camera.addCanvas3D( itsCanvas3D );

        camera.getPhysicalEnvironment( ).addInputDevice( itsInputDevice );
        InputDeviceBehavior inputDeviceBehavior = itsInputDevice.getBehavior( );

      // try also the OrbitManipultor, per Default the DefaultManipulator is set
      // you can change the orbit center by setting different coordinate values
      // for the Point3d
//        inputDeviceBehavior.setManipulator
//        (
//            new OrbitManipulator( new Point3d( 0.0,0.0,0.0 ) )
//        );

        // this will move the object
        inputDeviceBehavior.setTransformGroup( itsObjectTG );
        // this will move the camera
//        inputDeviceBehavior.setTransformGroup( itsCameraTG );

        // its wise to work in the local camera coordinate system; see what
        // happens if you comment it out
        inputDeviceBehavior.setLocalCoordinateSystemNode( platform );

        itsInitialBranchGroup.addChild( inputDeviceBehavior );

        itsInitialBranchGroup.addChild( itsObjectTG );
        itsInitialBranchGroup.addChild( itsCameraTG );

        itsInitialBranchGroup.compile( );
        itsLocale.addBranchGraph( itsInitialBranchGroup );
    }
*/

    public static void main(String[] args)
    {
        //change this to the appropriate serial port identifier
        VFXDriver inputDevice = VFXDriver.getVFXDriver();

        if(inputDevice != null)
        {
            JFrame window = new VFXDemo();
            window.setSize( 640, 400 );
            window.setVisible( true );
        }
    }
}