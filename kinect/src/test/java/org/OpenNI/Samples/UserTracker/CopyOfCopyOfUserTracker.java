/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package org.OpenNI.Samples.UserTracker;

import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SceneMetaData;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;

public class CopyOfCopyOfUserTracker extends Component {

	class NewUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("New user " + args.getId());
			try {
				if (skeletonCap.needPoseForCalibration()) {
					poseDetectionCap
					.startPoseDetection(calibPose, args.getId());
				} else {
					skeletonCap.requestSkeletonCalibration(args.getId(), true);
				}
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("Lost user " + args.getId());
			joints.remove(args.getId());

			/*			if(saved==false){
				//if user is Lose unexpectedly, ask for save data file
				final JFrame ask = new JFrame();
				JButton yes = new JButton("Yes");
				JButton no = new JButton("No");
				JLabel label = new JLabel("Do you want to save?");

				ask.add(label, BorderLayout.NORTH);
				ask.add(yes, BorderLayout.WEST);
				ask.add(no, BorderLayout.EAST);
				ask.setDefaultCloseOperation(ask.DISPOSE_ON_CLOSE);

				yes.setVisible(true);
				yes.setEnabled(true);
				yes.setSize(40, 20);
				no.setVisible(true);
				no.setEnabled(true);
				no.setSize(40, 20);

				ask.setVisible(true);
				ask.setSize(170, 70);

				dump = false;
				//for overwrite dump files
				flatCSVcreate = false;
				structCSVcreate = false;
				XMLcreate = false;

				//JFileChooser
				yes.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent arg0) {
						dumperFrame.setFileChooser();
						saved = true;
						if(dumperFrame.start.getText().equals("Record")){
							dumperFrame.area.setText("");
							dumperFrame.area.setEditable(true);
							dumperFrame.desc.setEnabled(true);
						}
						ask.setVisible(false);
					}

				});

				no.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						ask.setVisible(false);
					}

				});
			}
			 */			
		}		
	}

	class CalibrationCompleteObserver implements
	IObserver<CalibrationProgressEventArgs> {
		public void update(
				IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args) {
			System.out.println("Calibraion complete: " + args.getStatus());
			try {
				if (args.getStatus() == CalibrationProgressStatus.OK) {
					System.out.println("starting tracking " + args.getUser());
					skeletonCap.startTracking(args.getUser());
					joints.put(new Integer(args.getUser()),
							new HashMap<SkeletonJoint, SkeletonJointPosition>());
				} else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT) {
					if (skeletonCap.needPoseForCalibration()) {
						poseDetectionCap.startPoseDetection(calibPose,
								args.getUser());
					} else {
						skeletonCap.requestSkeletonCalibration(args.getUser(),
								true);
					}
				}
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}

	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args) {
			System.out.println("Pose " + args.getPose() + " detected for "
					+ args.getUser());
			try {
				poseDetectionCap.stopPoseDetection(args.getUser());
				skeletonCap.requestSkeletonCalibration(args.getUser(), true);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	}


	class DumperListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {

			if (e.getSource()==dumperFrame.start) {
				dump = true;
				saved = false;
				desc = false;
				dumperFrame.area.setEditable(false);
				dumperFrame.desc.setEnabled(false);
			}
			if (e.getSource()==dumperFrame.stop){
				
				if(dumperFrame.start.getText().equals("Record Pose")){
					dumperFrame.stop.setEnabled(true);
					dumperFrame.sitting.setEnabled(true);
					dumperFrame.standing.setEnabled(true);
					dumperFrame.crouched.setEnabled(true);
					dumperFrame.lyingDown.setEnabled(true);
					dumperFrame.fallen.setEnabled(true);
					dumperFrame.group1.clearSelection();
					selectedPose = false;
				}
				
				if(dumperFrame.start.getText().equals("Record SubPose")){
					dumperFrame.stop.setEnabled(true);
					dumperFrame.walking.setEnabled(true);
					dumperFrame.longArmed.setEnabled(true);
					dumperFrame.group2.clearSelection();
					selectedPose = false;
				}			
				
				dump = false;
				//for overwrite dump files
				flatCSVcreate = false;
				structCSVcreate = false;
				XMLcreate = false;

				//JFileChooser
				if(saved==false){
					dumperFrame.setFileChooser();
					saved = true;
				}
			}
			if(dumperFrame.start.getText().equals("Record")){
				dumperFrame.area.setText("");
				dumperFrame.area.setEditable(true);
				dumperFrame.desc.setEnabled(true);
			}
			if(e.getSource()==dumperFrame.standing){
				selectedPose=true;
				pose = "standing";
			}
			if(e.getSource()==dumperFrame.sitting){
				selectedPose=true;
				pose = "sitting";
			}
			if(e.getSource()==dumperFrame.crouched){
				selectedPose=true;
				pose = "crouched";
			}
			if(e.getSource()==dumperFrame.lyingDown){
				selectedPose=true;
				pose = "lyingDown";
			}
			if(e.getSource()==dumperFrame.fallen){
				selectedPose=true;
				pose = "fallen";
			}
			if(e.getSource()==dumperFrame.walking){
				selectedPose=true;
				pose = "walking";
			}
			if(e.getSource()==dumperFrame.longArmed){
				selectedPose=true;
				pose = "longArmed";
			}
		}
	}

	class UserDumperFrame extends JFrame{

		private JButton start;
		private JButton stop;
		private JComboBox combobox;
		private JPanel panel;
		private JFileChooser fileChooser;
		private JCheckBox desc;
		private JTextField area;
		private JPanel panelDesc;
		private JPanel panelPose, panelSubPose;
		private JRadioButton standing, sitting, crouched, lyingDown, fallen, walking, longArmed;
		private ButtonGroup group2;
		private ButtonGroup group1;
		//		private ExtensionFileFilter filter1;

		private String path = "/home/ste/Scrivania/AI/depict/kinect";

		public UserDumperFrame(){

			this.panel = new JPanel();

			this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);

			setComboBox();
			setButtoStart();
			setButtonStop();
			setPanelDesc();
			setPanelPose();
			setPanelSubPose();

			panel.add(combobox, BorderLayout.WEST);
			panel.add(start, BorderLayout.CENTER); 
			panel.add(stop, BorderLayout.EAST);
			panel.setVisible(true);

			this.add("North", panel);
			this.add("Center", panelDesc);
			this.pack();
			this.setVisible(true);


		}
		
		public void setPanelPose(){
			panelPose = new JPanel();
			standing = new JRadioButton("standing");
			sitting = new JRadioButton("sitting");
			crouched = new JRadioButton("crouched");
			lyingDown = new JRadioButton("lying down");
			fallen = new JRadioButton("fallen");
			
			
			group1 = new ButtonGroup();
		    group1.add(standing);
		    group1.add(sitting);
		    group1.add(crouched);
		    group1.add(lyingDown);
		    group1.add(fallen);
		    
		    panelPose.add(standing);
		    panelPose.add(sitting);
		    panelPose.add(crouched);
		    panelPose.add(lyingDown);
		    panelPose.add(fallen);
		    
			panelPose.setVisible(false);
		}
		
		public void setPanelSubPose(){
			panelSubPose = new JPanel();
			walking = new JRadioButton("walking");
			longArmed = new JRadioButton("long Armed");
			
			group2 = new ButtonGroup();
			group2.add(walking);
			group2.add(longArmed);
			
			panelSubPose.add(walking);
			panelSubPose.add(longArmed);
			
			panelSubPose.setVisible(false);
		}

		public void setPanelDesc(){

			panelDesc = new JPanel();
			desc = new JCheckBox("desc");
			//			area = new JTextArea(10, 35);
			area = new JTextField(35);

			desc.setEnabled(false);
			area.setEnabled(true);
			area.setEditable(false);

			desc.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					if(desc.isSelected())
						area.setEditable(true);
					else if(!desc.isSelected())
						area.setEditable(false);
				}
			});

			panelDesc.add(desc, BorderLayout.WEST);
			panelDesc.add(area, BorderLayout.EAST);

			desc.setVisible(true);
			area.setVisible(true);
			panelDesc.setVisible(false);
		}

		public void setFileChooser(){
			try{
				fileChooser = new JFileChooser(path);
				
				if(start.getText().equals("Record Pose") || start.getText().equals("Record SubPose")){
					String extensionFile = csv;
					
					int n = fileChooser.showSaveDialog(this);
					if (n == JFileChooser.APPROVE_OPTION) {

						File f = fileChooser.getSelectedFile();

						String flat = f.getName() + "." + extensionFile;

						File tempFlat = new File(flat);
						
						File f1 = new File(fileFlatCSV);
						
						f1.renameTo(tempFlat);
					}
				}
				
				if(start.getText().equals("Dump to CSV file")){
					
					String extensionFile = csv;
					int n = fileChooser.showSaveDialog(this);
					if (n == JFileChooser.APPROVE_OPTION) {

						File f = fileChooser.getSelectedFile();

						String flat = f.getName() + "Flat." + extensionFile;
						String struct = f.getName() + "Struct." + extensionFile;

						File tempFlat = new File(flat);
						File tempStruct = new File(struct);

						File f1 = new File(fileFlatCSV);
						File f2 = new File(fileStructCSV); 

						f1.renameTo(tempFlat);
						f2.renameTo(tempStruct);
					}
				}

				else if(start.getText().equals("Dump to XML file") || start.getText().equals("Record")){
					String extensionFile = xml;
					
					int n = fileChooser.showSaveDialog(this);
					if (n == JFileChooser.APPROVE_OPTION) {
						File f = fileChooser.getSelectedFile();

						String xml = f.getName() + "." + extensionFile;
						File tempXML = new File(xml);

						File f1 = new File(fileXML);
						f1.renameTo(tempXML);

					}
				}

				else if(start.getText().equals("Dump to XML and CSV file")){
					String extensionFile1 = csv;
					String extensionFile2 = xml;

					int n = fileChooser.showSaveDialog(this);
					if (n == JFileChooser.APPROVE_OPTION) {

						File f = fileChooser.getSelectedFile();

						String flat = f.getName() + "Flat."  + extensionFile1;
						String struct = f.getName() + "Struct."  + extensionFile1;
						String xml = f.getName() + "." + extensionFile2;

						File tempFlat = new File(flat);
						File tempStruct = new File(struct);
						File tempXML = new File(xml);

						File f1 = new File(fileFlatCSV);
						File f2 = new File(fileStructCSV);
						File f3 = new File(fileXML);

						f1.renameTo(tempFlat);
						f2.renameTo(tempStruct);
						f3.renameTo(tempXML);
					}
				}
			}catch (Exception ex) {}

		}

		private void setButtonStop(){
			this.stop = new JButton("Stop");

			stop.setVisible(true);
			stop.setEnabled(false);
		}

		private void setButtoStart(){

			this.start = new JButton("Dump");

			start.setVisible(true);
			start.setEnabled(false);
		}


		private void setComboBox(){

			String[] typeFile ={"", "create a CSV file", "create a XML file", "create CSV and XML file",
					"record SEQUENCE", "record POSE", "record SUB-POSE"};
			
			this.combobox = new JComboBox(typeFile);

			combobox.setSelectedIndex(0);
			combobox.setVisible(true);

			combobox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (combobox.getSelectedIndex()==0){
						start.setText("Dump");
						desc.setEnabled(false);
						start.setEnabled(false);
						panelDesc.setVisible(false);
						panelPose.setVisible(false);
						panelSubPose.setVisible(false);
					}
					else if (combobox.getSelectedIndex()==1){
						start.setText("Dump to CSV file");
						desc.setEnabled(false);
						panelDesc.setVisible(false);
						panelPose.setVisible(false);
						panelSubPose.setVisible(false);
						dumperFrame.pack();
					}
					else if (combobox.getSelectedIndex()==2){
						start.setText("Dump to XML file");
						desc.setEnabled(false);
						panelDesc.setVisible(false);
						panelPose.setVisible(false);
						panelSubPose.setVisible(false);
						dumperFrame.pack();
					}
					else if (combobox.getSelectedIndex()==3){
						start.setText("Dump to XML and CSV file");
						desc.setEnabled(false);
						panelDesc.setVisible(false);
						panelPose.setVisible(false);
						panelSubPose.setVisible(false);
						dumperFrame.pack();
					}
					else if (combobox.getSelectedIndex()==4){
						start.setText("Record");
						panelDesc.setVisible(true);
						desc.setEnabled(true);
						panelPose.setVisible(false);
						panelSubPose.setVisible(false);
						dumperFrame.pack();
					}
					else if (combobox.getSelectedIndex()==5){
						start.setText("Record Pose");
						desc.setEnabled(false);
						panelDesc.setVisible(false);
						dumperFrame.add("South", panelPose);
						panelPose.setVisible(true);
						panelSubPose.setVisible(false);
						dumperFrame.pack();
					}
					else if (combobox.getSelectedIndex()==6){
						start.setText("Record SubPose");
						desc.setEnabled(false);
						panelDesc.setVisible(false);
						dumperFrame.add("South", panelSubPose);
						panelSubPose.setVisible(true);
						panelPose.setVisible(false);
						dumperFrame.pack();
					}
				}
			});

		}
	}

	class DataTypeParser {

		/** Creates a new instance of DataTypeParser */
		public DataTypeParser() {
		}

		public boolean isNull(Object dataType) {
			return dataType==null;
		}
		public boolean isInteger(Object dataType) {
			return dataType instanceof Integer;
		}
		public boolean isBoolean(Object dataType) {
			return dataType instanceof Boolean;
		}
		public boolean isFloat(Object dataType) {
			return dataType instanceof Float;
		}
		public boolean isDouble(Object dataType) {
			return dataType instanceof Double;
		}
		public boolean isLong(Object dataType) {
			return dataType instanceof Long;
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
	private Context context;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private byte[] imgbytes;
	private float histogram[];
	String calibPose = null;
	HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

	private boolean drawBackground = true;
	private boolean drawPixels = true;
	private boolean drawSkeleton = true;
	private boolean printID = true;
	private boolean printState = true;

	private static int NMAX_JOINT = 24;
	private static String csv = "csv";
	private static String xml = "xml";

	private String fileFlatCSV;
	private PrintWriter writeFlatCSV;

	private String fileStructCSV;
	private PrintWriter writeStructCSV;

	private String fileXML;
	private DocumentBuilderFactory docFactory; 
	private DocumentBuilder docBuilder; 
	private Document doc;
	private Element rootElement;
	private Element sample;
	private Element joint;
	private Element x;
	private Element y;
	private Element z;
	private Element a;
	private TransformerFactory transformerFactory;
	private Transformer transformer;
	private DOMSource source;
	private StreamResult result;

	private Element description;

	private UserDumperFrame dumperFrame;
	private boolean dump;
	private boolean flatCSVcreate;
	private boolean structCSVcreate;
	private boolean XMLcreate;
	private boolean saved;
	private boolean desc;
	
	private String pose;
	private boolean selectedPose;
	
	private DumperListener dumperListener;

	private BufferedImage bimg;
	int width, height;

	private final String SAMPLE_XML_FILE = "/var/lib/ni/modules.xml";


	public CopyOfCopyOfUserTracker() {

		dumperListener = new DumperListener();
		dump = false;
		flatCSVcreate = false;
		structCSVcreate = false;
		XMLcreate = false;
		saved = false;
		desc = false;

		pose = "";
		selectedPose = false;
		
		//create an instance of UserdumperFrame
		dumperFrame = new UserDumperFrame();

		try {

			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

			depthGen = DepthGenerator.create(context);
			DepthMetaData depthMD = depthGen.getMetaData();

			histogram = new float[10000];
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();

			imgbytes = new byte[width * height * 3];

			userGen = UserGenerator.create(context);
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();

			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());
			skeletonCap.getCalibrationCompleteEvent().addObserver(
					new CalibrationCompleteObserver());
			poseDetectionCap.getPoseDetectedEvent().addObserver(
					new PoseDetectedObserver());

			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();

			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			context.startGeneratingAll();
		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		dumperFrame.start.addActionListener(dumperListener);
		dumperFrame.stop.addActionListener(dumperListener);
		
		dumperFrame.standing.addActionListener(dumperListener);
		dumperFrame.sitting.addActionListener(dumperListener);
		dumperFrame.crouched.addActionListener(dumperListener);
		dumperFrame.lyingDown.addActionListener(dumperListener);
		dumperFrame.fallen.addActionListener(dumperListener);
		dumperFrame.walking.addActionListener(dumperListener);
		dumperFrame.longArmed.addActionListener(dumperListener);
	}

	void initFlatCSVfile(){
		fileFlatCSV = "dumpSkeletonFlatCSV" + System.currentTimeMillis() + ".txt";
		flatCSVcreate = true;

		try {
			writeFlatCSV = new PrintWriter(fileFlatCSV);
			writeFlatCSV.println("\"idUSer\";\"timestamp\";\"code\";\"idJoint0\";\"x0\";\"y0\";\"z0\";\"a0\";" +
					"\"idJoint1\";\"x1\";\"y1\";\"z1\";\"a1\";" +
					"\"idJoint2\";\"x2\";\"y2\";\"z2\";\"a2\";" +
					"\"idJoint5\";\"x5\";\"y5\";\"z5\";\"a5\";" +
					"\"idJoint6\";\"x6\";\"y6\";\"z6\";\"a6\";" +
					"\"idJoint8\";\"x8\";\"y8\";\"z8\";\"a8\";" +
					"\"idJoint11\";\"x11\";\"y11\";\"z11\";\"a11\";" +
					"\"idJoint12\";\"x12\";\"y12\";\"z12\";\"a12\";" +
					"\"idJoint14\";\"x14\";\"y14\";\"z14\";\"a14\";" +
					"\"idJoint16\";\"x16\";\"y16\";\"z16\";\"a16\";" +
					"\"idJoint17\";\"x17\";\"y17\";\"z17\";\"a17\";" +
					"\"idJoint19\";\"x19\";\"y19\";\"z19\";\"a19\";" +
					"\"idJoint20\";\"x20\";\"y20\";\"z20\";\"a20\";" +
					"\"idJoint21\";\"x21\";\"y21\";\"z21\";\"a21\";" +
					"\"idJoint23\";\"x23\";\"y23\";\"z23\";\"a23\"");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	void initStructCSVfile(){
		fileStructCSV = "dumpSkeletonStructCSV" + System.currentTimeMillis() + ".txt";
		structCSVcreate = true;

		try {
			writeStructCSV = new PrintWriter(fileStructCSV);
			writeStructCSV.println("\"idUSer\";\"timestamp\";\"code\";\"idJoint\";\"x\";\"y\";\"z\";\"a\"");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	void writeFlush() {
		writeFlatCSV.flush();
		writeStructCSV.flush();
		//		writeXML.flush();
	}

	void initXMLfile(){
		fileXML = "dumpSkeletonXML" + System.currentTimeMillis() + ".xml";
		XMLcreate = true;

		try{
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			doc  = docBuilder.newDocument();
			rootElement = doc.createElement("session");
			doc.appendChild(rootElement);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
	}

	public void recordPoseDescription(){

		// description element
		description = doc.createElement("description");
		description.appendChild(doc.createTextNode(dumperFrame.area.getText()));
		rootElement.appendChild(description);

		desc = true;

	}

	void endXMLfile(){
		// write the content into xml file
		transformerFactory = TransformerFactory.newInstance();
		try {
			transformer = transformerFactory.newTransformer();

			source = new DOMSource(doc);
			result = new StreamResult(new File("/home/ste/Scrivania/AI/depict/kinect/" + fileXML));

			transformer.transform(source, result);

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}


	private void calcHist(ShortBuffer depth) {
		// reset
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		depth.rewind();

		int points = 0;
		while (depth.remaining() > 0) {
			short depthVal = depth.get();
			if (depthVal != 0) {
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++) {
			histogram[i] += histogram[i - 1];
		}

		if (points > 0) {
			for (int i = 1; i < histogram.length; i++) {
				histogram[i] = 1.0f - (histogram[i] / (float) points);
			}
		}
	}

	void updateDepth() {
		try {

			context.waitAnyUpdateAll();

			DepthMetaData depthMD = depthGen.getMetaData();
			SceneMetaData sceneMD = userGen.getUserPixels(0);

			ShortBuffer scene = sceneMD.getData().createShortBuffer();
			ShortBuffer depth = depthMD.getData().createShortBuffer();
			calcHist(depth);
			depth.rewind();

			while (depth.remaining() > 0) {
				int pos = depth.position();
				short pixel = depth.get();
				short user = scene.get();

				imgbytes[3 * pos] = 0;
				imgbytes[3 * pos + 1] = 0;
				imgbytes[3 * pos + 2] = 0;

				if (drawBackground || pixel != 0) {
					int colorID = user % (colors.length - 1);
					if (user == 0) {
						colorID = colors.length - 1;
					}
					if (pixel != 0) {
						float histValue = histogram[pixel];
						imgbytes[3 * pos] = (byte) (histValue * colors[colorID]
								.getRed());
						imgbytes[3 * pos + 1] = (byte) (histValue * colors[colorID]
								.getGreen());
						imgbytes[3 * pos + 2] = (byte) (histValue * colors[colorID]
								.getBlue());
					}
				}
			}
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	Color colors[] = { Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE };

	public void getJoint(int user, SkeletonJoint joint) throws StatusException {
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user,
				joint);
		if (pos.getPosition().getZ() != 0) {
			joints.get(user).put(
					joint,
					new SkeletonJointPosition(depthGen
							.convertRealWorldToProjective(pos.getPosition()),
							pos.getConfidence()));
		} else {
			joints.get(user).put(joint,
					new SkeletonJointPosition(new Point3D(), 0));
		}
	}

	public void getJoints(int user) throws StatusException {
		System.out.println("REading joints");
		getJoint(user, SkeletonJoint.HEAD);
		getJoint(user, SkeletonJoint.NECK);

		getJoint(user, SkeletonJoint.LEFT_SHOULDER);
		getJoint(user, SkeletonJoint.LEFT_ELBOW);
		getJoint(user, SkeletonJoint.LEFT_HAND);

		getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
		getJoint(user, SkeletonJoint.RIGHT_ELBOW);
		getJoint(user, SkeletonJoint.RIGHT_HAND);

		getJoint(user, SkeletonJoint.TORSO);

		getJoint(user, SkeletonJoint.LEFT_HIP);
		getJoint(user, SkeletonJoint.LEFT_KNEE);
		getJoint(user, SkeletonJoint.LEFT_FOOT);

		getJoint(user, SkeletonJoint.RIGHT_HIP);
		getJoint(user, SkeletonJoint.RIGHT_KNEE);
		getJoint(user, SkeletonJoint.RIGHT_FOOT);

	}

	void drawLine(Graphics g,
			HashMap<SkeletonJoint, SkeletonJointPosition> jointHash,
			SkeletonJoint joint1, SkeletonJoint joint2) {
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();

		if (jointHash.get(joint1).getConfidence() == 0
				|| jointHash.get(joint2).getConfidence() == 0)
			return;

		g.drawLine((int) pos1.getX(), (int) pos1.getY(), (int) pos2.getX(),
				(int) pos2.getY());
	}

	public void drawSkeleton(Graphics g, int user) throws StatusException {
		getJoints(user);
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
				.get(new Integer(user));

		drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
		drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER,
				SkeletonJoint.RIGHT_ELBOW);
		drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
		drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
		drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);

	}

	public void dumpSkeletonFlatCSV(PrintWriter writeFlatCSV, int user)
			throws StatusException {

		int cont=0;

		if(!flatCSVcreate){
			initFlatCSVfile();
		}

		else{
			Object[] orderedJoint = new Object [NMAX_JOINT];
			long timestamp = System.currentTimeMillis();

			if (writeFlatCSV == null)
				throw new IllegalArgumentException("File not valid: " + writeFlatCSV);

			getJoints(user);
			HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
					.get(new Integer(user));

			for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict
					.entrySet()){
				if (c.getValue().getConfidence()==1.0)
					orderedJoint[c.getKey().ordinal()] = c;
			}

			for(Object c : orderedJoint){
				if (c!=null)
					cont+=1;
			}

			if(cont==15){
				writeFlatCSV.print(user);
				writeFlatCSV.print(";");
				writeFlatCSV.print(timestamp);
				writeFlatCSV.print(";");
				writeFlatCSV.print(pose); 
				
				for(Object c : orderedJoint){
					if (c!=null){
						DataTypeParser d = new DataTypeParser();

						Entry<SkeletonJoint, SkeletonJointPosition> entry = (Entry<SkeletonJoint, SkeletonJointPosition>) c;

						writeFlatCSV.print(";");
						writeFlatCSV.print(entry.getKey().ordinal());
						writeFlatCSV.print(";");

						if (d.isFloat(entry.getValue().getPosition().getX()))
							writeFlatCSV.print(entry.getValue().getPosition().getX());
						else 
							writeFlatCSV.print((entry.getValue().getPosition().getX())/1000);	
						writeFlatCSV.print(";");

						if (d.isFloat(entry.getValue().getPosition().getY()))
							writeFlatCSV.print(entry.getValue().getPosition().getY());
						else
							writeFlatCSV.print((entry.getValue().getPosition().getY())/1000);
						writeFlatCSV.print(";");

						if (d.isFloat(entry.getValue().getPosition().getZ()))
							writeFlatCSV.print(entry.getValue().getPosition().getZ());
						else
							writeFlatCSV.print((entry.getValue().getPosition().getZ())/1000);
						writeFlatCSV.print(";");

						writeFlatCSV.print(entry.getValue().getConfidence());
					}
				}
				writeFlatCSV.println();
			}

		}
	}

	public void dumpSkeletonStructCSV(PrintWriter writeStructCSV, int user)
			throws StatusException {
		int cont=0;

		if(!structCSVcreate){
			initStructCSVfile();
		}
		else{
			Object[] orderedJoint = new Object [NMAX_JOINT];

			if (writeStructCSV == null)
				throw new IllegalArgumentException("File not valid: " + writeStructCSV);

			getJoints(user);
			HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints
					.get(new Integer(user));

			long timestamp = System.currentTimeMillis();

			for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict
					.entrySet()){
				if (c.getValue().getConfidence()==1.0)
					orderedJoint[c.getKey().ordinal()] = c;
			}

			for(Object c : orderedJoint){
				if (c!=null)
					cont+=1;
			}

			if(cont==15){
				for(Object c : orderedJoint){
					if (c!=null){
						DataTypeParser d = new DataTypeParser();

						Entry<SkeletonJoint, SkeletonJointPosition> entry = (Entry<SkeletonJoint, SkeletonJointPosition>) c;

						writeStructCSV.print(user);
						writeStructCSV.print(";");
						writeStructCSV.print(timestamp);
						writeStructCSV.print(";");
						writeStructCSV.print(pose);
						writeStructCSV.print(";");
						writeStructCSV.print(entry.getKey().ordinal());
						writeStructCSV.print(";");

						if (d.isDouble(entry.getValue().getPosition().getX()))
							writeStructCSV.print(entry.getValue().getPosition().getX());
						else 
							writeStructCSV.print((entry.getValue().getPosition().getX())/1000);	
						writeStructCSV.print(";");

						if (d.isDouble(entry.getValue().getPosition().getY()))
							writeStructCSV.print(entry.getValue().getPosition().getY());
						else
							writeStructCSV.print((entry.getValue().getPosition().getY())/1000);
						writeStructCSV.print(";");

						if (d.isDouble(entry.getValue().getPosition().getZ()))
							writeStructCSV.print(entry.getValue().getPosition().getZ());
						else
							writeStructCSV.print((entry.getValue().getPosition().getZ())/1000);
						writeStructCSV.print(";");

						writeStructCSV.println(entry.getValue().getConfidence());
					}
				}
			}		
		}
		//		writeStructCSV.println();
	}

	public void dumpSkeletonXML(int user){

		if(!XMLcreate){
			initXMLfile();
		}
		else{
			if(desc==false){
				recordPoseDescription();
			}

			Object[] orderedJoint = new Object [NMAX_JOINT];

			try {
				getJoints(user);
			} catch (StatusException e) {
				e.printStackTrace();
			}
			HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

			// sample elements
			sample = doc.createElement("sample");
			rootElement.appendChild(sample);

			// set attribute to sample element
			sample.setAttribute("id", user + "");
			sample.setAttribute("timestamp", System.currentTimeMillis() + "");

			// joints elements
			for (Map.Entry<SkeletonJoint, SkeletonJointPosition> c : dict.entrySet())
				orderedJoint[c.getKey().ordinal()] = c;

			for(Object c : orderedJoint){
				if (c!=null){
					Entry<SkeletonJoint, SkeletonJointPosition> entry = (Entry<SkeletonJoint, SkeletonJointPosition>) c;
					//joint element
					joint = doc.createElement("joint");
					sample.appendChild(joint);

					//set attribute to joint element
					joint.setAttribute("name", entry.getKey().toString());

					//x element
					x = doc.createElement("x");
					x.appendChild(doc.createTextNode(entry.getValue().getPosition().getX() + "" ));
					joint.appendChild(x);

					//y element
					y = doc.createElement("y");
					y.appendChild(doc.createTextNode(entry.getValue().getPosition().getY() + "" ));
					joint.appendChild(y);

					//z element
					z = doc.createElement("z");
					z.appendChild(doc.createTextNode(entry.getValue().getPosition().getZ() + "" ));
					joint.appendChild(z);

					//a element
					a = doc.createElement("a");
					a.appendChild(doc.createTextNode(entry.getValue().getConfidence() + "" ));
					joint.appendChild(a);
				}
			}

			endXMLfile();
		}

	}

	public void paint(Graphics g) {
		if (drawPixels) {
			DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width
					* height * 3);

			WritableRaster raster = Raster.createInterleavedRaster(dataBuffer,
					width, height, width * 3, 3, new int[] { 0, 1, 2 }, null);

			ColorModel colorModel = new ComponentColorModel(
					ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8,
						8, 8 }, false, false, ComponentColorModel.OPAQUE,
						DataBuffer.TYPE_BYTE);

			bimg = new BufferedImage(colorModel, raster, false, null);

			g.drawImage(bimg, 0, 0, null);
		}
		try {
			int[] users = userGen.getUsers();
			for (int i = 0; i < users.length; ++i) {

				Color c = colors[users[i] % colors.length];
				c = new Color(255 - c.getRed(), 255 - c.getGreen(),
						255 - c.getBlue());

				g.setColor(c);

				if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i])) {
					drawSkeleton(g, users[i]);
				}

				//if skeleton tracking....
				if (skeletonCap.isSkeletonTracking(users[i])){
					
					if(dumperFrame.start.getText().equals("Record Pose") ){

						if (!selectedPose){
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}
						else{
							if(!dump){
								dumperFrame.start.setEnabled(true);
								dumperFrame.combobox.setEnabled(true);
								dumperFrame.stop.setEnabled(false);
							}
							else{
								dumperFrame.start.setEnabled(false);
								dumperFrame.combobox.setEnabled(false);
								dumperFrame.stop.setEnabled(true);
								dumperFrame.sitting.setEnabled(false);
								dumperFrame.standing.setEnabled(false);
								dumperFrame.crouched.setEnabled(false);
								dumperFrame.lyingDown.setEnabled(false);
								dumperFrame.fallen.setEnabled(false);

								dumpSkeletonFlatCSV(writeFlatCSV, users[i]);
	//							dumpSkeletonStructCSV(writeStructCSV, users[i]);
								this.writeFlush();
							}
						}
					}

					else if(dumperFrame.start.getText().equals("Record SubPose")){
						
						if (!selectedPose){
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}
						else{
							if(!dump){
								dumperFrame.start.setEnabled(true);
								dumperFrame.combobox.setEnabled(true);
								dumperFrame.stop.setEnabled(false);
							}
							else{
								dumperFrame.start.setEnabled(false);
								dumperFrame.combobox.setEnabled(false);
								dumperFrame.stop.setEnabled(true);
								dumperFrame.walking.setEnabled(false);
								dumperFrame.longArmed.setEnabled(false);

								dumpSkeletonFlatCSV(writeFlatCSV, users[i]);
	//							dumpSkeletonStructCSV(writeStructCSV, users[i]);
								this.writeFlush();
							}
						}
					}
					
					else if (dumperFrame.start.getText()=="Dump to CSV file"){

						if(!dump){
							dumperFrame.start.setEnabled(true);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}

						else{
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(false);
							dumperFrame.stop.setEnabled(true);

							dumpSkeletonFlatCSV(writeFlatCSV, users[i]);
							dumpSkeletonStructCSV(writeStructCSV, users[i]);
							this.writeFlush();
						}
					}

					else if (dumperFrame.start.getText()=="Dump to XML file"){
						if(!dump){
							dumperFrame.start.setEnabled(true);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}

						else{
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(false);
							dumperFrame.stop.setEnabled(true);

							dumpSkeletonXML(users[i]);
						}
					}

					else if (dumperFrame.start.getText()=="Dump to XML and CSV file"){
						if(!dump){
							dumperFrame.start.setEnabled(true);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}

						else{
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(false);
							dumperFrame.stop.setEnabled(true);

							dumpSkeletonXML(users[i]);
							dumpSkeletonFlatCSV(writeFlatCSV, users[i]);
							dumpSkeletonStructCSV(writeStructCSV, users[i]);
							this.writeFlush();
						}
					}

					else if (dumperFrame.start.getText()=="Record"){
						if(!dump){
							dumperFrame.start.setEnabled(true);
							dumperFrame.combobox.setEnabled(true);
							dumperFrame.stop.setEnabled(false);
						}

						else{
							dumperFrame.start.setEnabled(false);
							dumperFrame.combobox.setEnabled(false);
							dumperFrame.stop.setEnabled(true);

							dumpSkeletonXML(users[i]);
						}
					}

					else{
						dumperFrame.start.setEnabled(false);
						dumperFrame.stop.setEnabled(false);
					}
				}

				//if skeleton lose ?



				if (printID) {
					Point3D com = depthGen.convertRealWorldToProjective(userGen
							.getUserCoM(users[i]));
					String label = null;
					if (!printState) {
						label = new String("" + users[i]);
					} else if (skeletonCap.isSkeletonTracking(users[i])) {
						// Tracking
						label = new String(users[i] + " - Tracking");
					} else if (skeletonCap.isSkeletonCalibrating(users[i])) {
						// Calibrating
						label = new String(users[i] + " - Calibrating");
					} else {
						// Nothing
						label = new String(users[i] + " - Looking for pose ("
								+ calibPose + ")");
					}

					g.drawString(label, (int) com.getX(), (int) com.getY());
				}
			}
		}
		catch (StatusException e) {
			e.printStackTrace();
		}
		catch(NullPointerException e1){

		}
	}



}

