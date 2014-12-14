package burtis.modules.gui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import burtis.common.mockups.Mockup;
import burtis.common.mockups.MockupBus;
import burtis.common.mockups.MockupBusStop;
import burtis.modules.gui.events.ConnectEvent;
import burtis.modules.gui.events.GoEvent;
import burtis.modules.gui.events.ProgramEvent;
import burtis.modules.gui.events.StepEvent;
import burtis.modules.gui.events.StopEvent;

public class View
{
    private final static Logger logger = Logger.getLogger(View.class.getName());
    private final AnimationPanel animationPanel;
    /** Kolejka, do ktorej wrzucamy obiekty odpowiadajace eventom */
    private final LinkedBlockingQueue<ProgramEvent> bQueue;
    private final BusStopInfoPanel busStopInfoPanel = new BusStopInfoPanel();
    private List<MockupBusStop> busStops;
    private final JPanel buttonPanel = new JPanel(new FlowLayout());
    private final JButton connectButton = new JButton("Connect");
    private long currentTime = 0;
    // private final JPanel dataPanel = new JPanel();
    private final JFrame frame;
    private final JButton goButton = new JButton("Go");
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private List<MockupBus> schedule;
    private final JScrollPane scrollPane = new JScrollPane();
    private final JSplitPane splitPaneHorizontal;
    // private JSplitPane splitPaneVertical;
    private final JButton stepButton = new JButton("Step");
    private final JButton stopButton = new JButton("Stop");
    private final JLabel timeLabel = new JLabel(Long.toString(currentTime));
    private BusStationButton tmpBusStationButton;
    private final JToolBar toolbar = new JToolBar();

    public View(LinkedBlockingQueue<ProgramEvent> bQueue,
            WindowListener exitListener)
    {
        this.bQueue = bQueue;
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame();
        if (exitListener == null)
        {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        else
        {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(exitListener);
        }
        frame.setLayout(new BorderLayout());
        frame.setTitle("burtis");
        frame.setVisible(true);
        frame.setSize(1400, 400);
        connectButton.addActionListener(e -> putInQueue(new ConnectEvent()));
        stopButton.addActionListener(e -> putInQueue(new StopEvent()));
        goButton.addActionListener(e -> putInQueue(new GoEvent()));
        stepButton.addActionListener(e -> putInQueue(new StepEvent()));
        toolbar.add(connectButton);
        toolbar.add(stopButton);
        toolbar.add(goButton);
        toolbar.add(stepButton);
        toolbar.add(timeLabel);
        toolbar.setRollover(true);
        animationPanel = new AnimationPanel(bQueue);
        JScrollPane scrollPaneAnimation = new JScrollPane(animationPanel);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPaneAnimation, BorderLayout.CENTER);
        splitPaneHorizontal = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, busStopInfoPanel);
        mainPanel.add(buttonPanel, BorderLayout.PAGE_START);
        mainPanel.add(scrollPaneAnimation, BorderLayout.CENTER);
        scrollPane.getViewport().add(mainPanel);
        frame.add(splitPaneHorizontal, BorderLayout.CENTER);
        frame.add(toolbar, BorderLayout.PAGE_START);
    }

    public void refresh(Mockup mockup)
    {
        schedule = mockup.getBuses();
        busStops = mockup.getBusStops();
        currentTime = mockup.getCurrentTime();
        timeLabel.setText(Long.toString(currentTime));
        for (MockupBusStop mbs : busStops)
        {
            String stationName = mbs.getName();
            tmpBusStationButton = new BusStationButton(stationName, bQueue);
            tmpBusStationButton.getButton().addActionListener(
                    this::onBusStationClicked);
            buttonPanel.add(tmpBusStationButton);
        }
        for (MockupBus bus : schedule)
        {
            animationPanel.addBus(new MockupBus(bus.getId()),
                    bus.getLengthPassed());
        }
    }

    public void updateBusInfoPanel(Integer i)
    {
        for (MockupBus bus : schedule)
        {
            if (bus.getId() == i)
            {
                busStopInfoPanel.setCurrentBus(i, bus.getPassengerList());
                return;
            }
        }
    }

    public void updateBusStopInfoPanel(String s)
    {
        for (MockupBusStop busStop : busStops)
        {
            if (busStop.getName() == s)
            {
                busStopInfoPanel.setCurrentBusStop(s,
                        busStop.getPassengerList());
                return;
            }
        }
    }

    private void onBusStationClicked(ActionEvent e)
    {
        String s = ((JButton) e.getSource()).getText();
        busStopInfoPanel.setCurrentBusStop(s);
    }

    private void putInQueue(ProgramEvent e)
    {
        try
        {
            bQueue.put(e);
        }
        catch (InterruptedException e1)
        {
            logger.log(Level.WARNING, "Couldn't put event in queue", e1);
        }
    }
}
