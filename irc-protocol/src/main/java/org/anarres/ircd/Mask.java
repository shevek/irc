package org.anarres.ircd;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Matcher;

import java.util.Objects;

/**
 * Parses a user/host mask.
 *
 * The following rules apply:
 * <pre>
 * foo = foo!*@*
 * foo!bar = foo!bar@*
 * foo@bar = *!foo@bar
 * foo!bar@baz = foo!bar@baz
 * </pre>
 */
public class Mask {

	private static final int	FLAGS =
			GlobCompiler.CASE_INSENSITIVE_MASK |
			GlobCompiler.READ_ONLY_MASK;

	private static final int	NICK = 0;
	private static final int	USER = 1;
	private static final int	HOST = 2;

	private String[]	text;
	private Pattern[]	pattern;

	public Mask(String mask)
						throws MalformedPatternException {
		this.text = split(mask);
		this.pattern = new Pattern[text.length];
		GlobCompiler	compiler = new GlobCompiler();
		for (int i = 0; i < text.length; i++)
			if (text[i] != null)
				pattern[i] = compiler.compile(text[i], FLAGS);
	}

	private String[] split(String text) {
		String[]	out = new String[3];

		int	idx0 = text.indexOf('!');
		int	idx1 = text.indexOf('@');

		if (idx0 == -1) {
			if (idx1 == -1) {
				out[NICK] = text;
				out[USER] = null;
				out[HOST] = null;
			}
			else {
				out[NICK] = null;
				out[USER] = text.substring(0, idx1);
				out[HOST] = text.substring(idx1 + 1);
			}
		}
		else {
			out[NICK] = text.substring(0, idx0);
			if (idx1 == -1) {
				out[USER] = text.substring(idx0 + 1);
				out[HOST] = null;
			}
			else {
				out[USER] = text.substring(idx0 + 1, idx1);
				out[HOST] = text.substring(idx1 + 1);
			}
		}

		return out;
	}

	private boolean match(String[] text) {
		Perl5Matcher	matcher = new Perl5Matcher();
		for (int i = 0; i < text.length; i++) {
			if (pattern[i] == null)
				continue;
			assert text[i] != null :
				"Matcher cannot match on null text in part " + i;
			if (!matcher.matches(text[i], pattern[i]))
				return false;
		}
		return true;
	}

	public boolean match(String input) {
		return match(split(input));
	}

	public boolean match(Client client) {
		return match(new String[] {
			client.getName(),
			client.getUsername(),
			client.getHostname()
		});
	}

	private String mask(String in) {
		return (in == null) ? "*" : in;
	}

	public int hashCode() {
		int	out = 0;
		for (int i = 0; i < text.length; i++)
			out ^= Objects.hashCode(text[i]);
		return out;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Mask))
			return false;
		Mask	m = (Mask)o;
		for (int i = 0; i < text.length; i++)
			if (!(Objects.equals(text[i], m.text[i])))
				return false;
		return true;
	}

	public String toString() {
		return mask(text[NICK]) + "!" +
				mask(text[USER]) + "@" + mask(text[HOST]);
	}

}
