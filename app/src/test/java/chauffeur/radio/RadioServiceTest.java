package chauffeur.radio;

import org.junit.jupiter.api.Test;

public class RadioServiceTest {
    RadioService classInTest = new RadioService();

    @Test
    void TestRadioService_Successful() {
        classInTest.GetPlaylists();
    }   
}
