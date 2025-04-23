package eu.solven.pepper.unittest;

public class InvalidJackson_noEmptyCtor {
	String someField;

	public InvalidJackson_noEmptyCtor(String string) {
		this.someField = string;
	}

	public String getSomeField() {
		return someField;
	}

	public void setSomeField(String someField) {
		this.someField = someField;
	}
}
