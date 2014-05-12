package prev_work;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class KeyboardEvents implements NativeKeyListener{

    
            public void nativeKeyPressed(NativeKeyEvent e) {
                    System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

                    if (e.getKeyCode() == NativeKeyEvent.VK_ESCAPE) {
                            GlobalScreen.unregisterNativeHook();
                    }
                    else if (e.getKeyCode() == NativeKeyEvent.VK_DEAD_ACUTE){
                        System.out.println("Gets Here");
                        //Calls screenshot function
                    }
            }

            public void nativeKeyReleased(NativeKeyEvent e) {
                    //System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
            }

            public void nativeKeyTyped(NativeKeyEvent e) {
                    //System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
            }

            public static void main(String[] args){
                    try {
                            GlobalScreen.registerNativeHook();
                    }
                    catch (NativeHookException ex) {
                            System.err.println("There was a problem registering the native hook.");
                            System.err.println(ex.getMessage());

                            System.exit(1);
                    }

                    //Construct the example object and initialze native hook.
                    GlobalScreen.getInstance().addNativeKeyListener(new KeyboardEvents());
            }
}
    
