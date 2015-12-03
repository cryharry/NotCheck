
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.synth.SynthSeparatorUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class NotCheck {
	Connection con;
	PreparedStatement pstmt;
	ResultSet rs;
	String sql,st_id="";
	ArrayList<String> list;
	
	// 학생검색 JFrame UI
	JFrame jFrame = new JFrame("학생검색");
	JTextField txtName = new JTextField(10);
	JPanel jPanel = new JPanel(new FlowLayout());
	DefaultTableModel defaultModel =
			new DefaultTableModel(new String[]{"바코드번호","학년","반","번호","이름"}, 0) {
		@Override
		public boolean isCellEditable(int row,int cloumn) {
			return false;
		}
	};
	JTable table = new JTable(defaultModel);
	JScrollPane jScrollPane = new JScrollPane(table);
	
	// 벌점 삭제 JFrame UI
	JFrame jFrame2 = new JFrame("벌점 삭제");
	JPanel jPanel2 = new JPanel(new FlowLayout());
	DefaultTableModel defaultModelChange =
			new DefaultTableModel(new String[]{"날짜","체크"}, 0) {
		@Override
		public boolean isCellEditable(int row,int cloumn) {
			return true;
		}
	};
	JTable table2 = new JTable(defaultModelChange){
		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
            case 0:
            	// 날짜 표시할 String 클래스
                return String.class;
            case 1:
            	// 체크박스 사용하기 위한 Boolean 클래스
                return Boolean.class;
            default:
                return String.class;
			}
		}		
	};
	JScrollPane jScrollPane2 = new JScrollPane(table2);
	JButton btnUpdate = new JButton("삭제");
	JCheckBox checkBox = new JCheckBox("전체 선택");
	
	
	public NotCheck() {
		table.getTableHeader().setReorderingAllowed(false);
		jPanel.add(new JLabel("이름"));
		jPanel.add(txtName);
		
		jFrame.setSize(500, 300);
		jFrame.setResizable(false);
		jFrame.setVisible(true);
		
		jFrame.add(jPanel,"North");
		jFrame.add(jScrollPane,"Center");
		txtName.requestFocus();
		// 텍스트필드 이벤트
		txtName.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			// 키보드 Enter 이벤트
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 10) {
					search();
				}
			}
		});
		// 테이블 더블클릭 이벤트
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// 마우스 더블클릭시
				if(e.getClickCount() == 2) {
					// 선택된 테이블의 바코드 번호를 가져와서 st_id 변수에 입력
					st_id = String.valueOf(defaultModel.getValueAt(table.getSelectedRow(),0));
					// 해당 바코드 학생의 등교 미체크 항목 검색 쿼리
					sql = "SELECT m_date FROM merit WHERE st_id ='"+st_id+"' and merit_code='69'";
					try {
						// DB 연결
						con = dbConn();
						// 쿼리 실행
						pstmt = con.prepareStatement(sql);
						rs = pstmt.executeQuery();
						ResultSetMetaData rsMetaData = rs.getMetaData();
						Object [] tempObject = new Object[rsMetaData.getColumnCount()]; 
						defaultModelChange.setRowCount(0);
						
						while(rs.next()) {
							for(int i=0; i < rsMetaData.getColumnCount(); i++) {
								tempObject[i] = rs.getDate(i+1);
							}
							defaultModelChange.addRow(tempObject);
						}
						
						if(defaultModelChange.getRowCount() > 0) {
							table2.setRowSelectionInterval(0, 0);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					} finally {
						tableChange();
					}					
				}
			}
		});
		// jFrame 닫기 버튼
		jFrame.setDefaultCloseOperation(jFrame.EXIT_ON_CLOSE);
	}
	
	// 벌점 삭제 Frame 메소드
	public void tableChange() {
		jPanel2.add(checkBox);
		jPanel2.add(btnUpdate);
		jFrame2.add(jPanel2,"North");
		jFrame2.add(jScrollPane2,"Center");
		jFrame2.pack();
		jFrame2.setVisible(true);
		JCheckBox tableChk = new JCheckBox();
		table2.getColumn("체크").setCellEditor(new DefaultCellEditor(tableChk));
		
		// 등교미체크 삭제 버튼 이벤트
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(table.getSelectedRowCount()) {
				case 0:
					JOptionPane.showMessageDialog(jFrame, "선택된 칼럼이 없습니다");
					break;
				default:
					try {
						con = dbConn();
						String date = "(";
						for(int i=0; i<table2.getRowCount(); i++) {
							if(table2.getValueAt(i, 1) != null && (Boolean)table2.getValueAt(i, 1) == true) {
									date += "'"+String.valueOf(table2.getValueAt(i, 0))+"'";
									if(i > 0) {
										date += ",";
									}
							}
						}
						date += ")";
						System.out.println(date);
						String sql = "DELETE FROM merit WHERE m_date in "+date+" and merit_code='69' and st_id='"+st_id+"'";
						Statement statement = con.createStatement();
						statement.executeUpdate(sql);
						sql = "DELETE FROM studentin WHERE inDate in "+date+" and state='69' and st_id='"+st_id+"'";
						statement = con.createStatement();
						statement.executeUpdate(sql);
						JOptionPane.showMessageDialog(jFrame, "변경완료");
						txtName.requestFocus();
						dbClose();
						jFrame2.setVisible(false);
					} catch (Exception err) {
						JOptionPane.showMessageDialog(jFrame, err.getMessage());
					}
					break;
				}
			}
		});
		
		// 전체 선택 checkbox 이벤트
		checkBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					for(int x=0, y= table2.getRowCount(); x<y; x++) {
						table2.setValueAt(new Boolean(true), x, 1);
					}
				} else {
					for(int x=0, y= table2.getRowCount(); x<y; x++) {
						table2.setValueAt(new Boolean(false), x, 1);
					}
				}
			}
		});
	}
	
	// KCM_IP.DAT 파일에서 라이센스 가져오기 메소드
	public ArrayList<String> fileRead() {
		list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("C:/Uni_Cool/KCM_IP.DAT"));
			String ip = br.readLine();
			String license = br.readLine();
			list.add(ip);
			list.add(license);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	// DB 접속 종료 메소드
	public void dbClose() {
		if(con!=null)try{con.close();}catch (Exception e) {}
		if(rs!=null)try{rs.close();}catch(Exception e){}
		if(pstmt!=null)try{pstmt.close();}catch(Exception e){}
		sql = "";
	}
	// DB 연결 메소드
	public Connection dbConn() throws Exception{
		ArrayList<String> list = fileRead();
		String DBurl = "jdbc:sqlserver://"+list.get(0)+":1433;databaseName="+list.get(1);
		String user = "sa";
		String pwd = "unicool";
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		con = DriverManager.getConnection(DBurl, user, pwd);
		return con;
	}
	
	// 학생 검색 메소드
	public void search(){
		try {
			con = dbConn();
			sql="select st_id,class,ban,num,name from student where name like ?";
			pstmt=con.prepareStatement(sql);
			pstmt.setString(1, '%'+txtName.getText().trim()+'%');
			//4단계 rs<=실행 저장 
			rs=pstmt.executeQuery();//데이터
			//
			ResultSetMetaData rsMetaData = rs.getMetaData();
			
			Object [] tempObject = new Object[rsMetaData.getColumnCount()];
			
			defaultModel.setRowCount(0);
			
			while(rs.next()) {
				for(int i=0; i < rsMetaData.getColumnCount(); i++) {
					tempObject[i] = rs.getString(i+1);
				}
				defaultModel.addRow(tempObject);
			}
			
			if(defaultModel.getRowCount() > 0) {
				table.setRowSelectionInterval(0, 0);
			}
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(jFrame, e.getMessage());
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(jFrame, e.getMessage());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(jFrame, e.getMessage());
		} finally {
			dbClose();
		}
	}
	
	// 메인 메소드
	public static void main(String[] args) {
		new NotCheck();
	}
		
}
