package uk.co.caprica.vlcj.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

public class PlayerControlsPanel extends JPanel {

  private static final int SKIP_TIME_MS = 10 * 1000;
  
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  private final MediaPlayer mediaPlayer;
  
  private JLabel timeLabel;
  private JProgressBar positionProgressBar;
  private JSlider positionSlider;
  private JLabel chapterLabel;
  
  private JButton previousChapterButton;
  private JButton rewindButton;
  private JButton stopButton;
  private JButton pauseButton;
  private JButton playButton;
  private JButton fastForwardButton;
  private JButton nextChapterButton;
  
  private JButton toggleMuteButton;
  private JSlider volumeSlider;
  
  private JButton captureButton;
  
  private JButton ejectButton;
  private JButton connectButton;
  
  private JButton fullScreenButton;
  
  private JButton subTitlesButton;
  
  private JFileChooser fileChooser;
  
  // Guard to prevent the position slider firing spurious change events when
  // the position changes during play-back - events are only needed when the 
  // user actually drags the slider and without the guard the play-back 
  // position will jump around
  private boolean setPositionValue;

  public PlayerControlsPanel(MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
    
    createUI();
    
    executorService.scheduleAtFixedRate(new UpdateRunnable(mediaPlayer), 0L, 1L, TimeUnit.SECONDS);
  }
  
  private void createUI() {
    createControls();
    layoutControls();
    registerListeners();
  }
   
  private void createControls() {
    timeLabel = new JLabel("hh:mm:ss");
    
    positionProgressBar = new JProgressBar();
    positionProgressBar.setMinimum(0);
    positionProgressBar.setMaximum(100);
    positionProgressBar.setValue(0);
    positionProgressBar.setToolTipText("Time");
    
    positionSlider = new JSlider();
    positionSlider.setMinimum(0);
    positionSlider.setMaximum(100);
    positionSlider.setValue(0);
    positionSlider.setToolTipText("Position");
    
    chapterLabel = new JLabel("00/00");
    
    previousChapterButton = new JButton();
    previousChapterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_start_blue.png")));
    previousChapterButton.setToolTipText("Go to previous chapter");

    rewindButton = new JButton();
    rewindButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_rewind_blue.png")));
    rewindButton.setToolTipText("Skip back");
    
    stopButton = new JButton();
    stopButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_stop_blue.png")));
    stopButton.setToolTipText("Stop");
    
    pauseButton = new JButton();
    pauseButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_pause_blue.png")));
    pauseButton.setToolTipText("Play/pause");
    
    playButton = new JButton();
    playButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_play_blue.png")));
    playButton.setToolTipText("Play");
    
    fastForwardButton = new JButton();
    fastForwardButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_fastforward_blue.png")));
    fastForwardButton.setToolTipText("Skip forward");

    nextChapterButton = new JButton();
    nextChapterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_end_blue.png")));
    nextChapterButton.setToolTipText("Go to next chapter");
    
    toggleMuteButton = new JButton();
    toggleMuteButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/sound_mute.png")));
    toggleMuteButton.setToolTipText("Toggle Mute");
    
    volumeSlider = new JSlider();
    volumeSlider.setOrientation(JSlider.HORIZONTAL);
    volumeSlider.setMinimum(0);
    volumeSlider.setMaximum(100);
    volumeSlider.setPreferredSize(new Dimension(100, 40));
    volumeSlider.setToolTipText("Change volume");
    
    captureButton = new JButton();
    captureButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/camera.png")));
    captureButton.setToolTipText("Take picture");
    
    ejectButton = new JButton();
    ejectButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/control_eject_blue.png")));
    ejectButton.setToolTipText("Load/eject media");
    
    connectButton = new JButton();
    connectButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/connect.png")));
    connectButton.setToolTipText("Connect to media");
    
    fileChooser = new JFileChooser();
    fileChooser.setApproveButtonText("Play");

    fullScreenButton = new JButton();
    fullScreenButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/image.png")));
    fullScreenButton.setToolTipText("Toggle full-screen");

    subTitlesButton = new JButton();
    subTitlesButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/comment.png")));
    subTitlesButton.setToolTipText("Cycle sub-titles");
  }
  
  private void layoutControls() {
    setBorder(new EmptyBorder(4, 4, 4, 4));
    
    setLayout(new BorderLayout());

    JPanel positionPanel = new JPanel();
    positionPanel.setLayout(new GridLayout(2, 1));
    positionPanel.add(positionProgressBar);
    positionPanel.add(positionSlider);
    
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout(8, 0));
    
    topPanel.add(timeLabel, BorderLayout.WEST);
    topPanel.add(positionPanel, BorderLayout.CENTER);
    topPanel.add(chapterLabel, BorderLayout.EAST);
    
    add(topPanel, BorderLayout.NORTH);
    
    JPanel bottomPanel = new JPanel();
    
    bottomPanel.setLayout(new FlowLayout());
    
    bottomPanel.add(previousChapterButton);
    bottomPanel.add(rewindButton);
    bottomPanel.add(stopButton);
    bottomPanel.add(pauseButton);
    bottomPanel.add(playButton);
    bottomPanel.add(fastForwardButton);
    bottomPanel.add(nextChapterButton);
    
    bottomPanel.add(volumeSlider);
    bottomPanel.add(toggleMuteButton);
    
    bottomPanel.add(captureButton);
    
    bottomPanel.add(ejectButton);
    bottomPanel.add(connectButton);

    bottomPanel.add(fullScreenButton);
    
    bottomPanel.add(subTitlesButton);
    
    add(bottomPanel, BorderLayout.SOUTH);
  }
  
  private void registerListeners() {
    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      @Override
      public void playing(MediaPlayer mediaPlayer) {
        updateVolume(mediaPlayer.getVolume());
      }
    });
    
    positionSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if(!positionSlider.getValueIsAdjusting() && !setPositionValue) {
          float positionValue = (float)positionSlider.getValue() / 100.0f;
          mediaPlayer.setPosition(positionValue);
        }
      }
    });
    
    previousChapterButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.previousChapter();
      }
    });

    rewindButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.skip(-SKIP_TIME_MS);
      }
    });

    stopButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.stop();
      }
    });
    
    pauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.pause();
      }
    });
    
    playButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.play();
      }
    });
    
    fastForwardButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.skip(SKIP_TIME_MS);
      }
    });
    
    nextChapterButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.nextChapter();
      }
    });
    
    toggleMuteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.mute();
      }
    });
    
    volumeSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if(!source.getValueIsAdjusting()) {
          mediaPlayer.setVolume(source.getValue());
        }
      }
    });
    
    captureButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.saveSnapshot();
      }
    });
    
    ejectButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
          mediaPlayer.playMedia(fileChooser.getSelectedFile().getAbsolutePath());
        }
      }
    });

    connectButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String mediaUrl = (String)JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(PlayerControlsPanel.this), "Enter a media URL", "Connect to media", JOptionPane.QUESTION_MESSAGE); 
        if(mediaUrl != null && mediaUrl.length() > 0) {
          mediaPlayer.playMedia(mediaUrl);
        }
      }
    });

    fullScreenButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mediaPlayer.toggleFullScreen();
      }
    });

    subTitlesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int spu = mediaPlayer.getSpu();
        if(spu > -1) {
          spu++;
          if(spu > mediaPlayer.getSpuCount()) {
            spu = -1;
          }
        }
        else {
          spu = 0;
        }
        mediaPlayer.setSpu(spu);
      }
    });
  }
  
  private final class UpdateRunnable implements Runnable {

    private final MediaPlayer mediaPlayer;
    
    private UpdateRunnable(MediaPlayer mediaPlayer) {
      this.mediaPlayer = mediaPlayer;
    }
    
    @Override
    public void run() {
      // FIXME shouldn't have to worry about exception here
      try {
        final long time = mediaPlayer.getTime();
        
        final long duration = mediaPlayer.getLength();
        final int position = duration > 0 ? (int)Math.round(100.0 * (double)time / (double)duration) : 0;
  
        final int chapter = mediaPlayer.getChapter();
        final int chapterCount = mediaPlayer.getChapterCount();
        
        // Updates to user interface components must be executed on the Event
        // Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateTime(time);
            updatePosition(position);
            updateChapter(chapter, chapterCount);
          }
        });
      }
      catch(Throwable t) {
      }
    }
  }
  
  private void updateTime(long millis) {
    String s = String.format("%02d:%02d:%02d",
      TimeUnit.MILLISECONDS.toHours(millis),
      TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), 
      TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    );
    timeLabel.setText(s);
  }

  private void updatePosition(int value) {
    positionProgressBar.setValue(value);
    
    // Set the guard to stop the update from firing a change event
    setPositionValue = true;
    positionSlider.setValue(value);
    setPositionValue = false;
  }
  
  private void updateChapter(int chapter, int chapterCount) {
    String s = chapterCount != -1 ? (chapter+1) + "/" + chapterCount : "-";
    chapterLabel.setText(s);
    chapterLabel.invalidate();
    validate();
  }
  
  private void updateVolume(int value) {
    volumeSlider.setValue(value);
  }
}
