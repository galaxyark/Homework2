//This file deal all options with Database
//class Hw2Connection create connection with database
//sqlstatement receive request from GUI and create sql statements to execute query.

import oracle.jdbc.*;
import oracle.spatial.geometry.*;
import oracle.sql.STRUCT;
import java.awt.Shape;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class SqlStatement{
	Statement stat;
	double[][] pointArray;
	boolean[] tableArray;
	int queryType;
	String sql;
	
	public SqlStatement(int query){
		stat = Hw2.hw2c.mainStat;
		tableArray = ControlPanel.activeTypePanel.getTypes();
		queryType = query;
		sql = new String();
	}
	
	private void phaseSQL(int type, List<double[]> list){
		Hw2.displayPanel.appendTextArea(sql);
		JGeometry j_geom;
		ResultSet rs;
		
		try{
			rs = stat.executeQuery(sql);
			while(rs.next()){
				STRUCT st = (oracle.sql.STRUCT)rs.getObject(1);
				j_geom = JGeometry.load(st);
				if(type==0){
					Hw2.imagePanel.appendBuilding(j_geom.createShape());
				}
				else if(type == 1){
					Hw2.imagePanel.appendFiredBuilding(j_geom.createShape());
				}
				else if(type == 2){
					list.add(j_geom.getPoint());
				}
				else if(type == 3){
					Hw2.imagePanel.checkFiredBuilding(j_geom.createShape(),rs.getNString(2));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//Do Whole Range query and Range query. 
	public void basicQuery(){
		Hw2.imagePanel.clearAll();
		String rangePointStr = new String();
		
		if(queryType == 1){
			JGeometry j = Hw2.imagePanel.getRaneJG();
			
			if(j==null){
				return;
			}else if(j.getOrdinatesArray()==null){
				return;
			}	
			
			double[] tmpPoints = j.getOrdinatesArray();
			
			for(double d:tmpPoints){
				rangePointStr=rangePointStr+ Double.toString(d)+",";
			}
			rangePointStr = rangePointStr+Double.toString(tmpPoints[0])+","+Double.toString(tmpPoints[1]);
		}
				
		if(tableArray[0]==true){
			if(queryType==0){
				sql = "SELECT B_SHAPE FROM BUILDING";
			}
			else if(queryType == 1){
				sql = "SELECT b.B_SHAPE FROM BUILDING b WHERE SDO_RELATE(b.B_SHAPE,SDO_GEOMETRY(2003,null,null,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY("+rangePointStr+")),'mask=ANYINTERACT')='TRUE'";					
			}
			phaseSQL(0,null);
		}
	 	
		if(tableArray[1]==true){
			if(queryType ==0 ){
				sql = "SELECT b.B_SHAPE FROM BUILDING b, FIREBUILDING f WHERE b.B_NAME = F_NAME";
			}
			else if(queryType==1){
				sql = "SELECT b.B_SHAPE FROM BUILDING b, FIREBUILDING f WHERE b.B_NAME = F_NAME AND SDO_RELATE(b.B_SHAPE,SDO_GEOMETRY(2003,null,null,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY("+rangePointStr+")),'mask=ANYINTERACT')='TRUE'";
			}
			phaseSQL(1,null);
		}
		
		if(tableArray[2]==true){
			if(queryType == 0){
 				sql = "SELECT H_SHAPE from HYDRANT";
 			}
 			else if(queryType == 1){
 				sql = "SELECT h.H_SHAPE FROM HYDRANT h WHERE SDO_RELATE(h.H_SHAPE,SDO_GEOMETRY(2003,null,null,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY("+rangePointStr+")),'mask=ANYINTERACT')='TRUE'";
 			}
			
			List<double[]> hydrantList = new ArrayList<double[]>();
			phaseSQL(2,hydrantList);
			
	 		pointArray = new double[hydrantList.size()][];
	 		hydrantList.toArray(pointArray);
	 		
	 		Hw2.imagePanel.updateHydrant(pointArray);
	 	}
	 	Hw2.imagePanel.repaint();
	}
		
	public void findNeighberBuilding(){
		Hw2.imagePanel.clearAll();
		sql = "SELECT b.B_SHAPE FROM BUILDING b, BUILDING b1, FIREBUILDING f WHERE b1.B_NAME = f.F_NAME AND SDO_WITHIN_DISTANCE(b.B_SHAPE,b1.B_SHAPE,'distance=100') = 'TRUE'";
		phaseSQL(0,null);

		sql = "SELECT b.B_SHAPE FROM BUILDING b, FIREBUILDING f WHERE b.B_NAME = f.F_NAME";
		phaseSQL(1,null);
		
		Hw2.imagePanel.repaint();		
	}
	
	public void findFireBuilding(){
		sql = "SELECT b.B_SHAPE FROM BUILDING b, FIREBUILDING f WHERE b.B_NAME = f.F_NAME";
		phaseSQL(1,null);	
		Hw2.imagePanel.repaint();
	}
	
	public void checkFiredBuilding(double[] point){
		sql = "SELECT b.B_SHAPE,b.B_NAME FROM BUILDING b, FIREBUILDING f WHERE b.b_NAME = f.F_NAME AND SDO_RELATE(b.B_SHAPE,SDO_GEOMETRY(2001,null,SDO_POINT_TYPE("+point[0]+","+point[1]+",null),null,null),'mask=ANYINTERACT')='TRUE'";
		phaseSQL(3,null);
		Hw2.imagePanel.repaint();
	}
	
	public void findClosetsFireHydrants(String[] building){
		if(building == null){
			return;
		}
		
		List<double[]> hydrantList = new ArrayList<double[]>();
		for(String s:building){
			sql = "SELECT h.H_SHAPE FROM HYDRANT h WHERE SDO_NN(h.H_SHAPE,(SELECT B_SHAPE FROM BUILDING WHERE B_NAME='"+s+"'),'sdo_num_res=1',1)='TRUE'";
			phaseSQL(2,hydrantList);
		}
		
		pointArray = new double[hydrantList.size()][2];
		hydrantList.toArray(pointArray);
		Hw2.imagePanel.updateHydrant(pointArray);
		Hw2.imagePanel.repaint();
	}
}

class Hw2Connection{
	public 
		Connection mainCon;
		Statement mainStat;
	
	public void startConnection(){
		try{
			System.out.println("Looking for Oracle's jdbc-odbc driver....");
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			System.out.println("Loaded");
			
			String url = "jdbc:oracle:thin:@localhost:1521:MyDataBase";
			String userId = "luf";
			String password = "tk1devil";
			
			System.out.println("Connecting to DB...");
			mainCon = DriverManager.getConnection(url,userId,password);
			System.out.println("connected!!");
			
			mainStat = mainCon.createStatement();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
		
	public void endConnection(){
		try{
			mainCon.commit();
			mainCon.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
}