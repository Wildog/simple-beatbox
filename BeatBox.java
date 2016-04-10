package space.limbo.BeatBox;

import space.limbo.BeatBox.Custom.BgPanel;
import space.limbo.BeatBox.Custom.GlassPanel;
import space.limbo.BeatBox.Custom.CustomSliderUI;
import space.limbo.BeatBox.Utils.WindowUtils;

import java.util.ArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** 
 * BeatBox 
 * A Simple Drum Machine
 * @author Wildog (i@wil.dog)
 * @version 1.0
 */
public class BeatBox {
    // mysql server server
    String server = "jdbc:mysql://localhost:3306/beatbox?"
        + "user=root&password=753951&useUnicode=true&characterEncoding=UTF8";

    // data containers
    ArrayList<JToggleButton> toggleButtonList;
    ArrayList<JButton> loginButtonList;
    ArrayList<String> authorList;
    DefaultListModel<String> projectListModel;
    JList<String> projectList;

    // some components
    JFrame theFrame;
    BgPanel background;
    GlassPanel glassPanel;
    JPanel mainPanel;
    JPanel[] tickGroups;
    JPanel loginPanel;
    JButton registerButton;
    JButton backwardButton;
    JButton forwardButton;
    JSlider slider;

    // midi components
    Sequencer sequencer;
    Sequence sequence;
    Track track;

    // sql connection
    Connection conn;

    // project infomations
    String title;
    String author;

    // helper parameters for UI
    int alpha            = 200;
    int increment        = 20;
    boolean started      = false;
    final int BUTTONSIZE = 75;

    // midi instruments
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
                                "Open Hi-Hat", "Acoustic Snare",
                                "Crash Cymbal", "Hand Clap",
                                "High Tom", "Hi Bongo",
                                "Maracas", "Whistle",
                                "Low Conga", "Cowbell",
                                "Vibraslap", "Low-mid Tom",
                                "High Agogo", "Open Hi Conga"};
    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.put("Slider.tickColor", Color.DARK_GRAY);
                    UIManager.put("Slider.foreground", Color.DARK_GRAY);
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new BeatBox().buildLoginScreen();
            }
        });
    }

    /** 
     * Setup MIDI and MySQL 
     */
    public BeatBox() {
        setUpMidi();
        connectDatabase();
    }

    /** 
     * Build initial login screen 
     */
    public void buildLoginScreen() {
        // the frame
        theFrame = new JFrame("Drum Machine Studio - " + "Login");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // the background
        BorderLayout layout = new BorderLayout();
        background = new BgPanel(layout);

        // the login panel
        loginPanel = new JPanel(new GridLayout(1, 5));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // backward button
        JButton backwardButton = new JButton("<");
        backwardButton.setFont(new Font("Courier", Font.PLAIN, 48));
        backwardButton.setForeground(Color.LIGHT_GRAY);
        backwardButton.setOpaque(false);
        backwardButton.setContentAreaFilled(false);
        backwardButton.setBorderPainted(false);
        backwardButton.addActionListener(new MyBackwardListener()); 
        backwardButton.addMouseListener(new MyMouseListener());

        // forward button
        JButton forwardButton = new JButton(">");
        forwardButton.setFont(new Font("Courier", Font.PLAIN, 48));
        forwardButton.setForeground(Color.LIGHT_GRAY);
        forwardButton.setOpaque(false);
        forwardButton.setContentAreaFilled(false);
        forwardButton.setBorderPainted(false);
        forwardButton.addActionListener(new MyForwardListener()); 
        forwardButton.addMouseListener(new MyMouseListener());

        // login buttons
        getAuthorList();
        loginPanel.add(backwardButton);
        loginButtonList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            JButton loginButton = new JButton(authorList.get(i), new ImageIcon("user.png"));
            loginButton.setSelectedIcon(new ImageIcon("user_sel.png"));
            loginButton.setFont(new Font("Courier", Font.PLAIN, 16));
            loginButton.setHorizontalTextPosition(SwingConstants.CENTER);
            loginButton.setVerticalTextPosition(SwingConstants.BOTTOM); 
            loginButton.setForeground(Color.LIGHT_GRAY);
            loginButton.setOpaque(false);
            loginButton.setContentAreaFilled(false);
            loginButton.setBorderPainted(false);
            loginButton.addActionListener(new MyLoginListener());
            loginButtonList.add(loginButton);
            loginPanel.add(loginButton);
        }
        loginPanel.add(forwardButton);
        loginPanel.setOpaque(false);

        // register button
        registerButton = new JButton("Register");
        registerButton.setForeground(Color.LIGHT_GRAY);
        registerButton.setOpaque(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.addActionListener(new MyRegisterListener()); 
        registerButton.addMouseListener(new MyMouseListener());
        registerButton.setHorizontalAlignment(SwingConstants.LEFT);

        // pack
        background.add(BorderLayout.SOUTH, registerButton); 
        background.add(BorderLayout.CENTER, loginPanel); 
        theFrame.getContentPane().add(background); 
        theFrame.setSize(680, 480);
        theFrame.setResizable(false);
        WindowUtils.displayOnCenter(theFrame);
        theFrame.setVisible(true);

        // dimmed mask and fade-in animation
        glassPanel = new GlassPanel();
        glassPanel.setOpaque(false);
        theFrame.setGlassPane(glassPanel);
        glassPanel.setVisible(true);
        new javax.swing.Timer(80, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (alpha > 0) {
                    alpha = alpha - increment;
                    increment = increment - 1;
                    if (increment <= 5) {
                        increment = 5;
                    }
                    glassPanel.setAlpha(alpha); 
                } else if (alpha == 0) {
                    glassPanel.setAlpha(alpha); 
                    javax.swing.Timer timer = (javax.swing.Timer) e.getSource();
                    timer.stop();
                    glassPanel.setVisible(false);
                }
            }
        }).start();
    }

    /** 
     * Build complete GUI after login 
     */
    public void buildGUI() {
        toggleButtonList = new ArrayList<JToggleButton>();
        Box controlBox = new Box(BoxLayout.Y_AXIS);
        Box switchBox = new Box(BoxLayout.X_AXIS);
        Box buttonBox = new Box(BoxLayout.X_AXIS);

        // start button
        JButton start = new JButton(new ImageIcon(((new ImageIcon(
            "switch.png").getImage()
            .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                    java.awt.Image.SCALE_SMOOTH)))));
        start.setSelectedIcon(new ImageIcon(((new ImageIcon(
            "switch_prs.png").getImage()
            .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                    java.awt.Image.SCALE_SMOOTH))))); 
        start.setPreferredSize(new Dimension(BUTTONSIZE, BUTTONSIZE));
        start.setOpaque(false);
        start.setContentAreaFilled(false);
        start.setBorderPainted(false);
        start.addActionListener(new MyStartListener());

        // clear button
        JButton clear = new JButton(new ImageIcon(((new ImageIcon(
            "clear.png").getImage()
            .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                    java.awt.Image.SCALE_SMOOTH)))));
        clear.setSelectedIcon(new ImageIcon(((new ImageIcon(
            "clear_sel.png").getImage()
            .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                    java.awt.Image.SCALE_SMOOTH))))); 
        clear.setPreferredSize(new Dimension(BUTTONSIZE, BUTTONSIZE));
        clear.setOpaque(false);
        clear.setContentAreaFilled(false);
        clear.setBorderPainted(false);
        clear.addActionListener(new MyClearListener());

        // pack switch box
        switchBox.add(start);
        switchBox.add(clear);

        // BPM slider
        slider = new JSlider(80, 160);  
        slider.setUI(new CustomSliderUI(slider));
        slider.setOpaque(false);
        slider.setSnapToTicks(true);  
        slider.setPaintTicks(true);  
        slider.setPaintLabels(true);  
        slider.setMajorTickSpacing(20);  
        slider.setMinorTickSpacing(10);  
        slider.setValue(120);
        slider.addChangeListener(new MyTempoListener());

        // projects list
        JPanel projectPanel = new JPanel();
        projectPanel.setLayout(new BorderLayout());
 
        projectListModel = new DefaultListModel<String>();
        getProjectList();
        projectList = new JList<String>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
        ListSelectionModel projectSelectionModel = projectList.getSelectionModel();
        projectSelectionModel.addListSelectionListener(new MySelectionListener());
 
        JScrollPane projectScrollPane = new JScrollPane(projectList);
        projectScrollPane.setPreferredSize(new Dimension(160, 200));
        projectPanel.add(projectScrollPane, BorderLayout.CENTER);
        projectPanel.setOpaque(false);
        projectPanel.setBorder(BorderFactory.createEmptyBorder(17, 5, 10, 0));

        // save button
        JButton saveButton = new JButton("Save");
        saveButton.setOpaque(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setBorderPainted(false); 
        saveButton.addActionListener(new MySaveListener());
        saveButton.addMouseListener(new MyMouseListener());
        saveButton.setForeground(Color.LIGHT_GRAY);
        saveButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        saveButton.setPreferredSize(new Dimension(40, 20));

        // load button
        JButton loadButton = new JButton("Load");
        loadButton.setOpaque(false);
        loadButton.setContentAreaFilled(false);
        loadButton.setBorderPainted(false); 
        loadButton.addActionListener(new MyLoadListener());
        loadButton.addMouseListener(new MyMouseListener());
        loadButton.setForeground(Color.LIGHT_GRAY);
        loadButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        loadButton.setPreferredSize(new Dimension(40, 20));
        
        // create button
        JButton createButton = new JButton("New");
        createButton.setOpaque(false);
        createButton.setContentAreaFilled(false);
        createButton.setBorderPainted(false); 
        createButton.addActionListener(new MyCreateListener());
        createButton.addMouseListener(new MyMouseListener());
        createButton.setForeground(Color.LIGHT_GRAY);
        createButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        createButton.setPreferredSize(new Dimension(40, 20));

        // delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.setOpaque(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setBorderPainted(false); 
        deleteButton.addActionListener(new MyDeleteListener()); 
        deleteButton.addMouseListener(new MyMouseListener());
        deleteButton.setForeground(Color.LIGHT_GRAY);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        deleteButton.setPreferredSize(new Dimension(40, 20));
        
        // pack button box
        buttonBox.add(saveButton);
        buttonBox.add(loadButton);
        buttonBox.add(createButton);
        buttonBox.add(deleteButton);
        buttonBox.setPreferredSize(new Dimension(160, 20));
        
        // pack control box
        controlBox.add(switchBox);
        controlBox.add(projectPanel);
        controlBox.add(buttonBox);
        controlBox.add(slider);
        controlBox.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 10));

        // name box
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            JLabel label = new JLabel(instrumentNames[i]);
            if (i == 0) {
                label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            } else {
                label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            }
            label.setForeground(Color.LIGHT_GRAY);
            nameBox.add(label);
        }
        nameBox.setBorder(BorderFactory.createEmptyBorder(110, 15, 10, 0));

        // main panel
        GridLayout grid = new GridLayout(1, 16, 1, 2);
        mainPanel = new JPanel(grid);

        tickGroups = new JPanel[16];
        for (int i = 0; i < 16; i++) {
            tickGroups[i] = new JPanel(new GridLayout(16, 1));
            tickGroups[i].setBackground(new Color(18, 113, 145));
            tickGroups[i].setOpaque(false); 
            mainPanel.add(tickGroups[i]);
        }
        for (int i = 0; i < 256; i++) {
            JToggleButton c = new JToggleButton();
            c.addChangeListener(new ToggleButtonListener());
            c.setSelected(true);
            toggleButtonList.add(c);
            tickGroups[i/16].add(c);
        }

        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(110, 5, 10, 0));

        // pack background
        background.add(BorderLayout.EAST, controlBox);
        background.add(BorderLayout.WEST, nameBox);
        background.add(BorderLayout.CENTER, mainPanel);
        background.setVisible(false);
        background.setVisible(true);
    }

    /** 
     * Set up MIDI Sequencer, Sequence and Track 
     */
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addMetaEventListener(new MyMetaListener());
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Connect MySQL server 
     */
    public void connectDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(server);
            conn.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Build and start the whole track (for 16 ticks)
     */
    public void buildTrackAndStart() {
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JToggleButton jc = toggleButtonList.get(i + (16*j));
                if (jc.isSelected() == false) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }

            try {
                // send MetaMessage when a tick is over
                track.add(new MidiEvent(new MetaMessage(1, ((Integer)i).toString().getBytes(), 1), i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            makeTrack(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        try {
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(slider.getValue());
            sequencer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Build the track (for a single tick); 
     * @param list trackList that controls a single tick
     */
    public void makeTrack(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(makeEvent(144, 9, key, 127, i));
                track.add(makeEvent(128, 9, key, 127, i+1));
            }
        }
    }

    /** 
     * Helper method to make MidiEvents 
     * 
     * @param comd command
     * @param chan channel
     * @param one data1
     * @param two data2
     * @param tick time point
     * @return MidiEvent
     */
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    /** 
     * Save project to database 
     * @param title project title
     * @param author project author
     */
    public void saveProject(String title, String author) {
        boolean[] buttonStates = new boolean[256];

        for (int i = 0; i < 256; i++) {
            JToggleButton jc = toggleButtonList.get(i);
            if (jc.isSelected()) {
                buttonStates[i] = true;
            }
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(buttonStates);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

            PreparedStatement pstmt = conn.prepareStatement("insert into projects values('" +
                    title + "', '" + author +"', ?, " + slider.getValue() + 
                    ") on duplicate key update data=values(data), bpm=" + sequencer.getTempoInBPM());
            pstmt.setBinaryStream(1,in,in.available());
            pstmt.execute();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Load project from database 
     * @param title project title
     * @param author project author
     */
    public void loadProject(String title, String author) {
        boolean[] buttonStates = new boolean[256];
        boolean hasResult = false;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from projects where title='" +
                    title + "'" + " and author='" + author + "'");
            while(rs.next()) {
                hasResult = true;
                InputStream in = rs.getBinaryStream("data");
                ObjectInputStream is = new ObjectInputStream(in);
                buttonStates = (boolean[]) is.readObject();
                int bpm = rs.getInt(4);
                slider.setValue(bpm);
                sequencer.setTempoInBPM(bpm);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (hasResult) {
            for (int i = 0; i < 256; i++) {
                JToggleButton jc = toggleButtonList.get(i);
                if (buttonStates[i]) {
                    jc.setSelected(true);
                } else {
                    jc.setSelected(false);
                }
            }
            theFrame.setTitle("Drum Machine Studio - " + title + " - " + author);
        }
    }

    /** 
     * Generate the list of projects from database
     */
    public void getProjectList() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select title from projects where author='" + 
                    author + "'");
            while(rs.next()) {
                projectListModel.addElement(rs.getString(1));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Generate the list of authors from database 
     */
    public void getAuthorList() {
        authorList = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select author from authors");
            while(rs.next()) {
                authorList.add(rs.getString(1));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Refresh the existing 3 login buttons 
     */
    public void refreshLoginButtons() {
        getAuthorList();
        for (int i = 0; i < 3; i++) {
            JButton jb = loginButtonList.get(i);
            jb.setText(authorList.get(i));
        }
    }

    /** 
     * Delete project from database and projectList
     * the main panel will be reseted when current project has been deleted
     */
    public void deleteProject() {
        int index = projectList.getMinSelectionIndex();
        String value = projectList.getSelectedValue();
        projectListModel.removeElementAt(index);
        if (title != null && title.equals(value)) {
            new Thread() {
                public void run() {
                    for (JToggleButton jc : toggleButtonList) {
                        jc.setSelected(true);
                    }
                }
            }.start(); 
        }
        try {
            PreparedStatement pstmt = conn.prepareStatement("delete from projects where title='" +
                    value + "' and author='" + author + "'");
            pstmt.execute();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * customized dimmed dialog 
     */
    class MyDialog extends Dialog implements ActionListener {
        static final long serialVersionUID = 123415231478124L;

        JLabel label;
        JTextField input = new JTextField(50);
        JTextField input2 = new JTextField(50);
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        int type;

        /** 
         * @param frame theFrame
         * @param modal isModal
         * @param type 1-Login; 2-Create Project; 3-Create User
         */
        MyDialog(Frame frame, boolean modal, int type) {
            super(frame, modal);
            this.type = type;
            if (type == 1) {
                label = new JLabel("Password for \"" + author + "\":");
            } else if (type == 2) {
                label = new JLabel("New project name:");
            } else if (type == 3) {
                label = new JLabel("Username & Password:");
            }
            setTitle(label.getText());
            setSize(260,140);
            setResizable(false);
            setLayout(null);
            label.setBounds(25, 30, 150, 20);
            add(label);
            input.setBounds(25, 60, 150, 30);
            add(input);
            if (type == 3) {
                input.setBounds(15, 60, 85, 30);
                input2.setBounds(100, 60, 85, 30);
                add(input2);
            }
            add(ok);
            add(cancel);
            ok.setBounds(25, 100, 70, 20);
            cancel.setBounds(100, 100, 70, 20);
            ok.addActionListener(this);
            cancel.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            String result = input.getText();
            if (e.getSource() == ok && !result.isEmpty()) {
                if (type == 1) {
                    // Login
                    try {
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("select password from authors where author='" + 
                                author + "'");
                        while(rs.next()) {
                            String password = rs.getString(1);
                            if (password.equals(result)) {
                                background.remove(loginPanel);
                                background.remove(registerButton);
                                buildGUI();
                                theFrame.setTitle("Drum Machine Studio - untitled - " + author);
                                dispose();
                                glassPanel.setAlpha(0);
                                glassPanel.setVisible(false);
                            } else {
                                input.setText("Wrong Password");
                            }
                        }
                        rs.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (type == 2) {
                    // Create Project
                    projectListModel.addElement(result);
                    projectList.setSelectedValue(result, true);
                    title = result;
                    theFrame.setTitle("Drum Machine Studio - " + title + " - " + author); 
                    dispose();
                    glassPanel.setAlpha(0);
                    glassPanel.setVisible(false);
                } else if (type == 3) {
                    // Create User 
                    String pwd = input2.getText();
                    try {
                        PreparedStatement pstmt = conn.prepareStatement("insert into authors values('" +
                                result + "', '" + pwd +"')");
                        pstmt.execute();
                        conn.commit();
                        dispose();
                        glassPanel.setAlpha(0);
                        glassPanel.setVisible(false);
                        refreshLoginButtons();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                dispose();
                glassPanel.setAlpha(0);
                glassPanel.setVisible(false);
            }
        }
    }

    class MyOptionPane {
        public MyOptionPane(int type) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    glassPanel.setVisible(true);
                    glassPanel.setAlpha(200);
                    MyDialog dialog = new MyDialog(theFrame, true, type);
                    dialog.setUndecorated(true);
                    dialog.setLayout(new BorderLayout());
                    dialog.add(Box.createRigidArea(new Dimension(200, 150)));
                    dialog.pack();
                    dialog.setLocationRelativeTo(theFrame);
                    dialog.setVisible(true);
                }
            });
        }
    }


    // Listeners


    class MyTempoListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            new Thread() {
                public void run() {
                    sequencer.setTempoInBPM(slider.getValue());
                }
            }.start();
        }
    }

    class MyMetaListener implements MetaEventListener {
        public void meta(MetaMessage msg) {
            if (msg.getType() == 0x2F) {
                // end of track
                // reset tick position and restart the track
                sequencer.setTickPosition(0);
                buildTrackAndStart();
            } else {
                // end of tick
                int currentTick = (int) sequencer.getTickPosition();
                int previousTick = (currentTick + 15) % 16;
                // repaint previous tick
                new Thread() {
                    public void run() {
                        tickGroups[previousTick].setOpaque(false);
                        tickGroups[previousTick].repaint();
                    }
                }.start();
                // repaint current tick
                new Thread() {
                    public void run() {
                        tickGroups[currentTick].setOpaque(true);
                        tickGroups[currentTick].repaint();
                    }
                }.start();
            }
        }
    }

    class MyForwardListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            int size = authorList.size();
            for (int i = 0; i < 3; i++) {
                JButton jb = loginButtonList.get(i);
                String origAuthor = jb.getText();
                int origLoc = authorList.indexOf(origAuthor);
                int newLoc = (origLoc + 3) % size;
                String newAuthor = authorList.get(newLoc);
                jb.setText(newAuthor);
            }
        }
    }
    
    class MyBackwardListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            int size = authorList.size();
            for (int i = 0; i < 3; i++) {
                JButton jb = loginButtonList.get(i);
                String origAuthor = jb.getText();
                int origLoc = authorList.indexOf(origAuthor);
                int newLoc = (origLoc + size - 3) % size;
                String newAuthor = authorList.get(newLoc);
                jb.setText(newAuthor);
            }
        }
    }

    class ToggleButtonListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent event) {
            JToggleButton jc = (JToggleButton) event.getSource();
            if (!jc.isSelected()){
                jc.setBackground(Color.LIGHT_GRAY);
            } else {
                jc.setBackground(Color.DARK_GRAY);
            }
        }
    };

    class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JButton jb = (JButton) event.getSource();
            if (!started){
                new Thread() {
                    public void run() {
                        jb.setIcon(new ImageIcon(((new ImageIcon(
                                "switch_prs.png").getImage()
                            .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                                java.awt.Image.SCALE_SMOOTH))))); 
                        buildTrackAndStart();
                    }
                }.start();
                started = true;
            } else {
                jb.setIcon(new ImageIcon(((new ImageIcon(
                        "switch.png").getImage()
                    .getScaledInstance(BUTTONSIZE, BUTTONSIZE,
                        java.awt.Image.SCALE_SMOOTH))))); 
                sequencer.stop();
                for (int i = 0; i < 16; i++) {
                    tickGroups[i].setOpaque(false);
                    tickGroups[i].repaint();
                }
                sequencer.setTickPosition(0);
                started = false;
            }
        }
    }

    class MyClearListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            new Thread() {
                public void run() {
                    for (JToggleButton jc : toggleButtonList) {
                        jc.setSelected(true);
                    }
                }
            }.start();
        }
    }

    class MySelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int index = projectList.getMinSelectionIndex();
            }
        }
    }

    class MySaveListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            title = projectList.getSelectedValue();
            if (title != null) {
                saveProject(title, author);  
            }
        }
    }
    
    class MyLoadListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            title = projectList.getSelectedValue();
            if (title != null) {
                loadProject(title, author); 
            }
        }
    }
    
    class MyLoginListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            JButton jb = (JButton) a.getSource();
            author = jb.getText();
            new MyOptionPane(1);
        }
    }

    class MyCreateListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            new MyOptionPane(2);
        }
    }

    class MyRegisterListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            new MyOptionPane(3);
        }
    }

    class MyDeleteListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            deleteProject();
        }
    }

    class MyMouseListener implements MouseListener {
        public void mouseReleased(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            JButton jb = (JButton) e.getSource();
            jb.setForeground(Color.LIGHT_GRAY);
        }

        public void mouseEntered(MouseEvent e) {
            JButton jb = (JButton) e.getSource();
            jb.setForeground(new Color(57,105,138).brighter());
        }
    }

}
