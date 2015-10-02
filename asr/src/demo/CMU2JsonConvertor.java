package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class CMU2JsonConvertor {
	private static String replace(String regex, String actual) {
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(actual);

		Set<String> repls = new HashSet<>();

		while (match.find()) {
			String val = match.group();
			if (repls.add(val)) {
				String repl = val.replaceAll("\\Q.\\E", ". ");
				actual = actual.replace(val, repl);
			}
		}
		return actual;
	}

	public static void generateJson() throws Exception {

		System.out.println("Please provide the path to actual bible text file");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String actual = in.readLine();

		BufferedReader read = new BufferedReader(new FileReader(actual));
		String ab = null;
		StringBuffer sb = new StringBuffer();
		while ((ab = read.readLine()) != null) {
			sb.append(ab);
		}

		ab = sb.toString().replaceAll("\\d+", "").replaceAll(",[^']", ", ")
				.replaceAll("\\Q.\\E\"", ".\" ").replaceAll("'\"\\s+", "'\"")
				.replaceAll("-", " ");

		ab = replace("\\w+\\Q.\\E'\"", ab);
		ab = replace("\\w+\\Q.\\E\\w+", ab);
		ab = replace("\\W\\Q.\\E\\w+", ab);

		List<String> vals = Arrays.asList(ab.split("\\s+"));

		String line = null;

		JsonFactory jfactory = new JsonFactory();

		System.out
				.println("Please provide the path to store the output json , example C:/test/out.json");

		String dir1 = in.readLine();

		System.out.println("Please provide the path to the MDF file");

		String dir2 = in.readLine();

		JsonGenerator jGenerator = jfactory.createJsonGenerator(new File(dir1),
				JsonEncoding.UTF8);
		jGenerator.writeStartObject();
		int counter = 0;
		jGenerator.writeFieldName("Verses");
		jGenerator.writeStartArray();
		try (BufferedReader bf = new BufferedReader(new FileReader(dir2))) {
			while ((line = bf.readLine()) != null) {

				if (line.startsWith("+"))
					continue;

				String listVal = vals.get(counter);
				++counter;

				System.out.println(listVal);
				System.out.println(line);

				if (line.contains("[")) {

					String[] arr = line.split("\\s+");

					String val = arr[2].substring(1, arr[2].length() - 1);
					String[] start_end = val.split(":");
					String start = start_end[0];
					String end = start_end[1];

					jGenerator.writeStartObject();
					jGenerator.writeFieldName("word");
					jGenerator.writeUTF8String(listVal.getBytes(), 0,
							listVal.length());
					jGenerator.writeFieldName("strt");
					jGenerator.writeUTF8String(start.getBytes(), 0,
							start.length());
					jGenerator.writeFieldName("end");
					jGenerator.writeUTF8String(end.getBytes(), 0, end.length());
					jGenerator.writeEndObject();

				} else {
					jGenerator.writeStartObject();
					jGenerator.writeFieldName("word");
					jGenerator.writeUTF8String(listVal.getBytes(), 0,
							listVal.length());
					jGenerator.writeFieldName("strt");
					jGenerator.writeUTF8String("".getBytes(), 0, 0);
					jGenerator.writeFieldName("end");
					jGenerator.writeUTF8String("".getBytes(), 0, 0);
					jGenerator.writeEndObject();
				}

			}
			jGenerator.writeEndArray();
			jGenerator.writeEndObject();
			jGenerator.flush();
		}

		read.close();

	}
}