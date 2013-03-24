//GUI part the program
//Include all components of the User interface
//receive the events from user and send request to Hw2Functions

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import oracle.spatial.geometry.*;
import oracle.jdbc.*;

//components to chose the Active Feature Type
class ActiveTypePanel extends JPanel{
	boolean[] tableArr;
	
	JCheckBox
		cb1 = new JCheckBox("Buildings"),
		cb2 = new JCheckBox("Buildings on fire"),
		cb3 = new JCheckBox("Hydrants");
	
	public ActiveTypePanel(){
		tableArr = new boolean[3];
		setPreferredSize(new Dimension(210,160));
		
		cb1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setTypes(cb1,0);
			}
		});
		cb2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setTypes(cb2,1);
			}
		});
		cb3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setTypes(cb3,2);
			}
		});

		add(cb1);
		add(cb2);
		add(cb3); 
	
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createTitledBorder("Active Feature Type"));
	}
	
	private void setTypes(JCheckBox cb, int tableIndex){
		if(cb.isSelected()){
			tableArr[tableIndex] = true;
		}
		else{
			tableArr[tableIndex] = false;
		}
	}
	
	public boolean[] getTypes(){
		return tableArr;
	}
}

//Panel contains query rad
class QueryPanel extends JPanel{
	private int whichQuery = 0;
	private JRadioButton
		rb1 = new JRadioButton("Whole Region", true),
		rb2 = new JRadioButton("Range Query", false),
		rb3 = new JRadioButton("Find Neighbor Buildings", false),
		rb4 = new JRadioButton("Find Closets Fire Hydrants", false);		
	private ButtonGroup queryRadioButtonGroup;

	private ActionListener al = new ActionListener(){
		public void actionPerformed(ActionEvent e){
			createQueryNum(((JRadioButton)e.getSource()).getText());
			queryHandler();
		}	
	};
	
	public QueryPanel(){
		setPreferredSize(new Dimension(210,200));
		setBorder(BorderFactory.createTitledBorder("Query"));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		rb1.addActionListener(al);
		rb2.addActionListener(al);
		rb3.addActionListener(al);
		rb4.addActionListener(al);
		
		//Add four button into a ButtonGroup to make them mutual
		queryRadioButtonGroup = new ButtonGroup();
		queryRadioButtonGroup.add(rb1);
		queryRadioButtonGroup.add(rb2);
		queryRadioButtonGroup.add(rb3);
		queryRadioButtonGroup.add(rb4);

		//Add to the panel as usual
		add(rb1);
		add(rb2);
		add(rb3);
		add(rb4);
	}
	
	private void createQueryNum(String str){
		if(str.equals("Whole Region")){
			whichQuery = 0;
		}
		else if(str.equals("Range Query")){
			whichQuery = 1;
		}
		else if(str.equals("Find Neighbor Buildings")){
			whichQuery = 2;
		}
		else if(str.equals("Find Closets Fire Hydrants")){
			whichQuery = 3;
		}	
	}
	
	private void queryHandler(){
		//Changing radio button will reset the image. 
		Hw2.imagePanel.clearAll();	
		//Other radio button will clear the range points
		if(whichQuery!=1){ 
			Points range = Hw2.imagePanel.getRange();
			range.clear();
		}
		
		Hw2.imagePanel.repaint();
		
		if(whichQuery==3){
			SqlStatement sqlStatement = new SqlStatement(whichQuery);
			sqlStatement.findFireBuilding();
		}
	}
	
	public int getQueryType(){
		return whichQuery;
	}
}

//Panel contains a submit button
class SubmitPanel extends JPanel{
	JButton submitButton;
	
	public SubmitPanel(){
		setPreferredSize(new Dimension(210, 200));
		//setBorder(BorderFactory.createLineBorder(Color.green));
		
		submitButton = new JButton("Submit Query");
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int query = ControlPanel.queryPanel.getQueryType();				
				SqlStatement sqlStatement = new SqlStatement(ControlPanel.queryPanel.getQueryType());
				if(query==0||query==1){
					sqlStatement.basicQuery();
				}
				else if(query==2){
					sqlStatement.findNeighberBuilding();
				}
				else if(query==3){
					sqlStatement.findClosetsFireHydrants(Hw2.imagePanel.getCheckedFiredBuilding());
				}
			}
		});
		add(BorderLayout.CENTER,submitButton);
	}
}

//Control components
class ControlPanel extends JPanel{
	static ActiveTypePanel activeTypePanel;
	static QueryPanel queryPanel;
	static SubmitPanel submitPanel;
	
	public ControlPanel(){
		setPreferredSize(new Dimension(210,90));
		//setBorder(BorderFactory.createLineBorder(Color.red, 2));
		
		activeTypePanel = new ActiveTypePanel();
		add(BorderLayout.NORTH, activeTypePanel);
		
		queryPanel = new QueryPanel();
		add(BorderLayout.CENTER, queryPanel);
		
		submitPanel = new SubmitPanel();
		add(BorderLayout.SOUTH, submitPanel);
	}
}


class MouseMethods implements MouseInputListener{
	public void mouseClicked(MouseEvent e){
		//System.out.println("cl");
		//do nothing
	}
	
	public void mouseEntered(MouseEvent e){
		//System.out.println("en");
		//do nothing
	}
	
	public void mouseExited(MouseEvent e){
		//System.out.println("ex");
		//do nothing
	}
	
	public void mousePressed(MouseEvent e){
		int query = ControlPanel.queryPanel.getQueryType();
		
		if(query==1){
			Points range = Hw2.imagePanel.getRange();
			if(e.getButton()==1){
				double[] point = new double[2];	
				point[0] = e.getX();
				point[1] = e.getY();
				range.appendPoints(point);
				Hw2.imagePanel.setMouseButton(1);
			}
			else if(e.getButton()==3){
				Hw2.imagePanel.setMouseButton(3);
				Hw2.imagePanel.createRange();
			}
			
			Hw2.imagePanel.repaint();
		}	
		
		if(query==3){
			if(e.getButton()==1){
				double[] point = new double[2];
				point[0] = e.getX();
				point[1] = e.getY();
				SqlStatement sqlStatement = new SqlStatement(query);
				sqlStatement.checkFiredBuilding(point);
			}
			else if(e.getButton()==3){
				Hw2.imagePanel.clearCheckedFiredbuilding();
			}
		}
	}
	
	public void mouseReleased(MouseEvent e){
		//System.out.println("rel");
		//do nothing
	}
	
	public void mouseMoved(MouseEvent e){
		Hw2.displayPanel.setMousePosition(e.getX(),e.getY());
	}
	public void mouseDragged(MouseEvent e){
		//do nothing
	}
}

class Points{
	private 
		List<double[]> pointsList;
		double[][] points;
	
	public Points(){
		pointsList = new ArrayList<double[]>();
	}
	
	public void appendPoints(double[] point){
		pointsList.add(point);
	}
	
	public double[][] getPointArr(){
		points = new double[pointsList.size()][];
		pointsList.toArray(points);
		return points;
	}
	
	public int getSize(){
		return pointsList.size();
	}
	
	public void clear(){
		pointsList.clear();
	}
}

// JPanel contains map.jpg
class ImagePanel extends JPanel{	
	double[][] hydrant; 
	GeneralPath building;
	GeneralPath firedBuilding;
	GeneralPath checkedFiredBuildingPath;
	GeneralPath rangePath;
	Points rangePoints;
	JGeometry rangeJG;
	int ele[] = {1,1003,1};
	int whichButton = 1;		//1:left; 2:middle; 3:right
	List<String> checkedFiredBuilding;
	
	public ImagePanel(){
		//load image
		try{
			this.setLayout(null);
			
			BufferedImage myPicture = ImageIO.read(new File("map.jpg"));
			JLabel picLabel = new JLabel(new ImageIcon(myPicture));
			setPreferredSize(new Dimension(820,580));
			picLabel.setBounds(0, 0, 820, 580);
			add(picLabel);
			checkedFiredBuilding = new ArrayList<String>();
			
		}catch(IOException ex){
			System.out.println("Image IO Error");
		}
		
		//setBorder(BorderFactory.createLineBorder(Color.red, 2));
		
		building = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		firedBuilding = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		rangePath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		checkedFiredBuildingPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		
		rangePoints = new Points();
		//Add mouse event 
		MouseMethods m = new MouseMethods();
		this.addMouseListener(m);
		this.addMouseMotionListener(m);		
	}

	//Add a building shape to GeneralPah object.
	public void appendBuilding(Shape shape){
		building.append(shape, false);
	}	

	public void clearBuilding(){
		building.reset();
	}
	
	public void appendFiredBuilding(Shape shape){
		firedBuilding.append(shape, false);
	}	
	
	public void clearFiredBuilding(){
		firedBuilding.reset();
	}
	
	public void checkFiredBuilding(Shape building, String name){
		if(checkedFiredBuilding.contains(name)){
			checkedFiredBuilding.remove(name);
		}
		else{
			checkedFiredBuilding.add(name);
		}
		
		checkedFiredBuildingPath.append(building, false);
	}
	
	public String[] getCheckedFiredBuilding(){
		String[] s = new String[checkedFiredBuilding.size()];
		checkedFiredBuilding.toArray(s);	
		return s;
	}
	
	public void clearCheckedFiredbuilding(){
		checkedFiredBuilding.clear();
		checkedFiredBuildingPath.reset();
	}
	
	//Update hydrant points 
	public void updateHydrant(double[][] hydrant){
		this.hydrant = hydrant;
	}
	
	//clear hydrant points
	public void clearHydrant(){
		hydrant = null;
	}
	
	public void setMouseButton(int button){
		whichButton = button;
	}
	
	public void createRange(){
		double[] tmpPoints = new double[rangePoints.getSize()*2];
		double[][] rangP = rangePoints.getPointArr();
		System.out.println(rangePoints.getSize());
		
		for(int i=0;i<rangP.length;i++){
			tmpPoints[i*2] = rangP[i][0];
			tmpPoints[i*2+1] = rangP[i][1];
		}
		
		rangeJG = new JGeometry(2003,0,ele,tmpPoints);
		rangePath.append(rangeJG.createShape(), false);
	}
	
	public Points getRange(){
		return rangePoints;
	}
	
	public JGeometry getRaneJG(){
		return rangeJG;
	}
	
	public void clearAll(){
		clearBuilding();
		clearHydrant();
		clearFiredBuilding();
		clearCheckedFiredbuilding();
	}
	//paint function draws result shapes on the image panel
	//the repaint() function in other places will call this paint() function
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		//draw building 
		g2.setPaint(Color.yellow);
		g2.draw(building);
		
		//draw fired building
		Stroke stroke = new BasicStroke(5.0f);
		g2.setStroke(stroke);
		g2.setPaint(Color.red);
		g2.draw(firedBuilding);	
		
		//fill or unfill checked fired building
		g2.fill(checkedFiredBuildingPath);
		
		//draw all hydrants
		if(hydrant!=null){
			g2.setPaint(Color.green);
			for(double[] h:hydrant){
				g2.fill(new Rectangle2D.Double(h[0], h[1], 15, 15));
			}
		}
		
		//draw points of range area
		if(rangePoints.getSize()!=0){
			g2.setColor(Color.red);
			if(whichButton == 1){			//left button, draw points	
				for(double[] d:rangePoints.getPointArr()){
					g2.fill(new Rectangle2D.Double(d[0], d[1], 5, 5));
				}
			}
			else if(whichButton == 3){		//right button, draw the frame
				g2.draw(rangePath);
				rangePath.reset();
				rangePoints.clear();
			}
		}
	}
}

//Display mouse coordinates and SQL statement
class DisplayPanel extends JPanel{
	private JLabel mousePosition;
	private TextArea sqlArea;
	private JScrollPane scrollPane;
	private int queryCount;
		
	public DisplayPanel(){
		queryCount =1;
		setPreferredSize(new Dimension(1050,100));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		mousePosition = new JLabel("Current mouse location:(000,000)");
		sqlArea = new TextArea(3,145);
		scrollPane = new JScrollPane(sqlArea);
		
		add(BorderLayout.NORTH,mousePosition);
		add(BorderLayout.SOUTH,sqlArea);
		add(scrollPane);
		//setBorder(BorderFactory.createLineBorder(Color.red, 2));
	}
	
	public void setMousePosition(int x, int y){
		mousePosition.setText("Current mouse location:("+x+","+y+")");
	}
	
	public void appendTextArea(String str){
		sqlArea.append("Query "+queryCount+":"+str+"\n");
		queryCount=queryCount+1;
	}
}

class Hw2 extends JFrame{
	JPanel contentPane;
	static ImagePanel imagePanel;
	static DisplayPanel displayPanel;
	static ControlPanel controlPanel;
	static Hw2Connection hw2c;
	
	public Hw2()
	{
		contentPane = (JPanel)this.getContentPane();
		setSize(1050,720);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		setResizable(false);
		
		hw2c = new Hw2Connection();
		hw2c.startConnection();	
	}
	
	public static void main(String[] args){	
		Hw2 myhw2 = new Hw2();
		
		//Add panel that display the map
		imagePanel = new ImagePanel();
		myhw2.add(BorderLayout.WEST, imagePanel);
		//Add control pad component
		controlPanel = new ControlPanel();
		myhw2.add(BorderLayout.EAST, controlPanel);		
		//Add display panel to display position of mouse and SQL statement.
		displayPanel = new DisplayPanel();
		myhw2.add(BorderLayout.SOUTH, displayPanel);

		myhw2.setVisible(true);
	}
}