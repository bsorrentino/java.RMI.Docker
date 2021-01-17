package test.common;

import java.io.Serializable;

public interface AnotherObject extends Serializable {
	
	void test();
	
	int getNumber();
	
	void gimmeYourListener(ListenerTest listener);
	
}
