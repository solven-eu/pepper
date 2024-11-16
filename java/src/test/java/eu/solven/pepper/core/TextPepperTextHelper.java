package eu.solven.pepper.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.pepper.core.PepperStringHelper;

public class TextPepperTextHelper {

	@Test
	public void testAnonymizeWeirdOnlyWord() {
		String output = PepperStringHelper.simplify("ᵿ");
		Assertions.assertThat(output).isEqualTo("_");
	}

	@Test
	public void testSimplify() {
		Assertions
				.assertThat(PepperStringHelper.simplify(
						"   Jean-Pierre\r\n\rçà émis\nêtre  où\tBATEAU\r\n\r\nIdentifiant national de compte\r\n"))
				.isEqualTo("   Jean-Pierre  ca emis etre  ou BATEAU  Identifiant national de compte ");
	}

	@Test
	public void testNormalize() {
		Assertions
				.assertThat(PepperStringHelper.normalize(
						"   Jean-Pierre\rçà émis\nêtre  où\tBATEAU\r\n\r\nIdentifiant national de compte\r\n"))
				.isEqualTo("jean-pierre ca emis etre ou bateau identifiant national de compte");
	}

	@Test
	public void testSimplify_Spanish() {
		Assertions.assertThat(PepperStringHelper.simplify("A função")).isEqualTo("A funcao");
	}
}
