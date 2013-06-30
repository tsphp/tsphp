/*
 * Source code copied from http://www.java2s.com/Code/Java/Swing-JFC/CreatingTextAreawithUndoRedoCapabilities.htm
 * 
 */
package ch.tutteli.tsphp.demo;

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/*
 * Source code copied from http://www.java2s.com/Code/Java/Swing-JFC/CreatingTextAreawithUndoRedoCapabilities.htm
 * 
 */
class UndoableTextArea extends JTextArea implements UndoableEditListener, FocusListener, KeyListener
{

    private UndoManager m_undoManager;

    public UndoableTextArea() {
        this(new String());
    }

    public UndoableTextArea(String text) {
        super(text);
        getDocument().addUndoableEditListener(this);
        this.addKeyListener(this);
        this.addFocusListener(this);
    }

    private void createUndoMananger() {
        m_undoManager = new UndoManager();
        m_undoManager.setLimit(10);
    }

    private void removeUndoMananger() {
        m_undoManager.end();
    }

    @Override
    public void focusGained(FocusEvent fe) {
        createUndoMananger();
    }

    @Override
    public void focusLost(FocusEvent fe) {
        removeUndoMananger();
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        m_undoManager.addEdit(e.getEdit());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown())) {
            try {
                m_undoManager.undo();
            } catch (CannotUndoException cue) {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown())) {
            try {
                m_undoManager.redo();
            } catch (CannotRedoException cue) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
