/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package puzzlegameframe;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.geom.*;
import java.lang.Integer;

public class PuzzleGameFrame extends Frame implements ActionListener, MouseListener, MouseMotionListener, ItemListener{
    private MenuBar menuBar;
    private Menu fileMenu, editMenu, viewMenu;
    private MenuItem mi;
    Button btn;
    List list;
    Dialog win, open, empty;
    Label labelpath, label;
    TextArea text;
    private int[][] grid = new int[4][4];
    BufferedImage image;
    BufferedImage[] images = new BufferedImage[15];
    String filename;
    File f;
    int h=100, w=100, emptysquares=1;
    Point selectedsquare, checksquare;
    String path=".";
    boolean dontchange=false, close=true;
    CheckboxMenuItem lines, numbers, drag;
    public PuzzleGameFrame(){
        super("Sliding Puzzle Game");
        setBounds(0,0,500,500);
        
        //Create GUI
        setMenuBar(menuBar = new MenuBar());
        menuBar.add(fileMenu=new Menu("File"));
        fileMenu.add(mi=new MenuItem("Open"));
        mi.addActionListener(this);
        fileMenu.add(mi=new MenuItem("Close"));
        mi.addActionListener(this);
        fileMenu.add(mi=new MenuItem("Exit"));
        mi.addActionListener(this);
        menuBar.add(editMenu=new Menu("Edit"));
        editMenu.add(mi=new MenuItem("Shuffle"));
        mi.addActionListener(this);
        editMenu.add(mi=new MenuItem("Sort"));
        mi.addActionListener(this);
        editMenu.add(drag=new CheckboxMenuItem("Drag Mode"));
        drag.addItemListener(this);
        editMenu.add(mi=new MenuItem("Empty Squares"));
        mi.addActionListener(this);
        menuBar.add(viewMenu=new Menu("View"));
        viewMenu.add(lines=new CheckboxMenuItem("Lines", true));
        lines.addItemListener(this);
        viewMenu.add(numbers=new CheckboxMenuItem("Numbers", true));
        numbers.addItemListener(this);
        
        //Dialog Box "win".
        win = new Dialog(this, "You Win!", false);
        win.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                win.setVisible(false);
            }
        });
        win.setSize(150, 100);
        win.add(new Label("You Win!"),BorderLayout.CENTER);
        
        //Dialog Box "open".
        open = new Dialog(this,"Opening a File", false);
        open.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                open.setVisible(false);
            }
        });
        open.setSize(300,300);
        list = new List(10);
        list.addActionListener(this);
        open.add(labelpath = new Label(path), BorderLayout.NORTH);
        getFileList();
        open.add(list,BorderLayout.CENTER);
        
        //Dialog Box "Empty".
        //Allows for user to have more than one empty square.
        empty = new Dialog(this,"Click Your Choice", false);
        empty.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                empty.setVisible(false);
                emptysquares = Integer.parseInt(text.getText());
                initBoard();
                repaint();
                if (emptysquares>0) drag.setState(true);
                else drag.setState(false);
            }
        });
        empty.setSize(125,110);
        empty.add(label = new Label("Enter # Empty(<=3)"),BorderLayout.NORTH);
        empty.add(text = new TextArea("0",1,5,text.SCROLLBARS_NONE),BorderLayout.CENTER);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false); dispose();
                System.exit(0);
            }
        });
        initBoard();
    }
    //Get file list for the dialog box "Open".
    public void getFileList(){
        f = new File(path+"/");
        String[] filelist = f.list();
        list.removeAll();
        for (int i=0;i<filelist.length;i++){
            list.add(filelist[i]);
        }
        labelpath.setText(path);
    }
    
    //Open file. If directory load files to list and change path.
    //If gif file, split into 16 subimages and set width and hieght.
    //Add subimages to list images[].
    public void openFile(String filename){
        f = new File(path+"/"+filename);
        if (filename.substring(filename.length()-3, filename.length()).equals("gif"))
            System.out.println("Loaded "+filename);
        else if (f.isDirectory()){
            path=path+"/"+filename;
            getFileList();
            return;
        }
        else
            return;
        try {
            image = ImageIO.read(f);
        }
        catch (Exception e){
            System.out.println(e);
            System.exit(0);
        }
        w = image.getWidth()/4;
        h = image.getHeight()/4;
        int m=0;
        for (int r=0;r<=3;r++){
            for (int c=0;c<=3;c++) {
                if (m==15){break;}
                images[m] = image.getSubimage(c*w, r*h, w, h);
                m++;
            }
        }
        lines.setState(true);
        numbers.setState(false);
        close=false;
        repaint();
    }
    
    //Read grid[][] to define output
    public void paint(Graphics g){
        for (int r=0;r<=3;r++){
            for (int c=0;c<=3;c++){
                if (grid[r][c]==0){
                    //empty square
                    g.setColor(Color.gray);
                    g.fillRect(w*c+50, h*r+50,w,h);
                }
                else{
                    if (!close)
                        //images
                        g.drawImage(images[(grid[r][c])-1],w*c+50,h*r+50,null);
                    if (numbers.getState()){
                        //Numbers
                        g.setColor(Color.black);
                        g.drawString(""+grid[r][c],(50+w/2)+w*c,(50+h/2)+h*r);
                    }
                }
            }
        }
        if (lines.getState()){
            for (int i=0;i<=4;i++){
                //Lines
                g.setColor(Color.black);
                g.drawLine(50,i*h+50,w*4+50,i*h+50);
                g.drawLine(i*w+50, 50, i*w+50, (h*4)+50);
            }
        }
        if (selectedsquare!=null){
            //Paint Selected Square with red border
            g.setColor(Color.red);
            g.drawRect(selectedsquare.x*w+50, selectedsquare.y*h+50,w,h);
            if (checksquare!=null){
                //Paint check square with red border
                g.setColor(Color.red);
                g.drawRect(checksquare.x*w+50, checksquare.y*h+50,w,h);
            }
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        String item = ae.getActionCommand();
        if (item.equals("Open")){
            open.setVisible(true);
            openFile(path);
        }
        else if (item.equals("Close")){
            close=true;
            lines.setState(true);
            numbers.setState(true);
            w=100;
            h=100;
            repaint();
        }
        else if (item.equals("Exit")){
            setVisible(false);
            dispose();
            System.exit(0);
        }
        else if (item.equals("Shuffle")){
            shuffleBoard();
            repaint();
        }
        else if (item.equals("Sort")){
            initBoard();
            repaint();
        }
        else if (item.equals("Empty Squares")){
            empty.setVisible(true);
        }
        if (ae.getSource() == list) {
            String listitem = list.getSelectedItem();
            System.out.println(listitem);
            if (listitem != null && item.length() > 0){
                openFile(listitem);
            }
        }
    }
    public void itemStateChanged(ItemEvent e) {
        repaint();
    }
    public void mouseClicked(MouseEvent e) {
        if (checkValid(e.getPoint())){
            if (playerMove(getNumAt(getSquareAt(e.getPoint())))){
                repaint();
            }
        }
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
        //Check if dragging is selected and valid area on the plane
        if (drag.getState()&&checkValid(e.getPoint())){
            dontchange=true;
            //Selects if an empty square is next to it and if it is not empty
            if(playerMove(getNumAt(getSquareAt(e.getPoint())))&&!checkEmpty(0, getNumAt(getSquareAt(e.getPoint())))){
                selectedsquare= getSquareAt(e.getPoint());
                repaint();
            }
            dontchange=false;
        }
    }
    public void mouseReleased(MouseEvent e) {
        if (drag.getState()){
            if (selectedsquare!=null){
                if (checksquare!=null){
                    //Move selected and check square
                    checkEmpty(getNumAt(selectedsquare), getNumAt(checksquare));
                    checksquare=null;
                }
                selectedsquare=null;
                repaint();
            }
        }
    }
    public void mouseDragged(MouseEvent e) {
        if (drag.getState()){
            if(selectedsquare!=null){
                dontchange=true;
                //Selects checksquare if it is valid on the plane, not the selected square, and not an already checked square.
                //This avoids repaint while it is being dragged.
                if (checkValid(e.getPoint())&&!getSquareAt(e.getPoint()).equals(selectedsquare)&&!getSquareAt(e.getPoint()).equals(checksquare)&&checkEmpty(0, getNumAt(getSquareAt(e.getPoint())))){
                    checksquare = getSquareAt(e.getPoint());
                    repaint();
                }
                dontchange=false;
            }
        }
    }
    public void mouseMoved(MouseEvent e) {
    }
    
    private Point getSquareAt(Point p){
        //Returns row and collumn as y and x
        return new Point((p.x-50)/w,(p.y-50)/h);
        
    }
    private int getNumAt(Point p){
        //returns number as it  0   1   2   3
        //is on the grid like   4   5   6   7
        //so:                   8   9   10  11
        //                      12  13  14  15
        if (p.y==0)
            return p.x;
        else if (p.y==1)
            return 4+p.x;
        else if (p.y==2)
            return 8+p.x;
        else if (p.y==3)
            return 12+p.x;
        else
            return 0;
    }
    public boolean checkValid(Point p){
        //Checks if point is on the image or plane
        if (p.x<(w*4)+50&&p.x>50&&p.y<(h*4)+50&&p.y>50){
            return true;
        }
        return false;
    }
    
    //Creates grid in order
    public void initBoard(){
        int m = 0;
        for (int r=0;r<=3;r++){
            for (int c=0;c<=3;c++){
                m++;
                grid[r][c] = m;
            }
        }
        grid[3][3] = 0;
        if (emptysquares>1){
            grid[3][2] = 0;
            if (emptysquares>2){
                grid[3][1] = 0;
                if (emptysquares>3){
                    grid[3][0] = 0;
                }
            }
        }
        
    }
    public void shuffleBoard(){
        //Standard Shuffle
        //Only difference is that a 0 is forced on to the grid[3][3]
        int m, n;
        for (int r=0;r<=3;r++){
            for (int c=0;c<=3;c++){
                if (grid[r][c]==0){
                    grid[r][c]=grid[3][3];
                    grid[3][3]=0;
                }
                do{
                    m=(int)(Math.random()*3);
                    n=(int)(Math.random()*3);
                }while(m==3&&n==3);
                if (r == 3 && c ==3)
                    break;
                int temp=grid[r][c];
                grid[r][c] = grid[m][n];
                grid[m][n]=temp;
            }
        }
    }
    public boolean playerMove(int square){
        //Checks all squares around the clicked square and returns true once found
        if (square==0){
            return checkEmpty(0,1)||checkEmpty(0,4);
        }
        else if(square==1){
            return checkEmpty(1,0)||checkEmpty(1,5)||checkEmpty(1,2);
        }
        else if(square==2){
            return checkEmpty(2,1)||checkEmpty(2,6)||checkEmpty(2,3);
        }
        else if(square==3){
            return checkEmpty(3,2)||checkEmpty(3,7);
        }
        else if(square==4){
            return checkEmpty(4,0)||checkEmpty(4,5)||checkEmpty(4,8);
        }
        else if(square==5){
            return checkEmpty(5,1)||checkEmpty(5,4)||checkEmpty(5,6)||checkEmpty(5,9);
        }
        else if(square==6){
            return checkEmpty(6,2)||checkEmpty(6,5)||checkEmpty(6,7)||checkEmpty(6,10);
        }
        else if(square==7){
            return checkEmpty(7,3)||checkEmpty(7,11)||checkEmpty(7,6);
        }
        else if(square==8){
            return checkEmpty(8,4)||checkEmpty(8,12)||checkEmpty(8,9);
        }
        else if(square==9){
            return checkEmpty(9,8)||checkEmpty(9,10)||checkEmpty(9,5)||checkEmpty(9,13);
        }
        else if(square==10){
            return checkEmpty(10,9)||checkEmpty(10,11)||checkEmpty(10,6)||checkEmpty(10,14);
        }
        else if(square==11){
            return checkEmpty(11,15)||checkEmpty(11,10)||checkEmpty(11,7);
        }
        else if(square==12){
            return checkEmpty(12,8)||checkEmpty(12,13);
        }
        else if(square==13){
            return checkEmpty(13,12)||checkEmpty(13,14)||checkEmpty(13,9);
        }
        else if(square==14){
            return checkEmpty(14,13)||checkEmpty(14,10)||checkEmpty(14,15);
        }
        else {
            return checkEmpty(15,14)||checkEmpty(15,11);
        }
    }
    private boolean checkEmpty(int target, int check){
        //If check is empty switch with the clicked square
        if (grid[getRow(check)][getCol(check)] == 0){
            if (dontchange){
                dontchange=false;
                return true;
            }
            grid[getRow(check)][getCol(check)]=grid[getRow(target)][getCol(target)];
            grid[getRow(target)][getCol(target)]=0;
            if (gameOver()){
                win.setVisible(true);
            }
            return true;
        }
        return false;
    }
    public boolean gameOver(){
        int m=0;
        for (int r=0;r<=3;r++){
            for (int c=0;c<=3;c++){
                m++;
                if (r==3 & c==3)
                    break;
                if (grid[r][c]!=m)
                    return false;
            }
        }
        return true;
    }
    public int getCol(int n){
        return n%4;
    }
    public int getRow(int n){
        return n/4;
    }
    
    public static void main(String[] args){
        PuzzleGameFrame pgf = new PuzzleGameFrame();
        pgf.setVisible(true);
    }
}
