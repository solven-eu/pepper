package eu.solven.pepper.unittest;

public class InvalidJackson_weirdSetter {
	String someField;

	public InvalidJackson_weirdSetter() {
		this.someField = "defaultField";
	}

	public InvalidJackson_weirdSetter(String string) {
		this.someField = string;
	}

	public String getSomeField() {
		return someField;
	}

	// A weird setter so that verifyJackson will panic
	public void setSomeField(String someField) {
		this.someField = someField + "_suffix";
	}
}
