package io.cucumber.gherkin;

import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat;
import io.cucumber.messages.internal.com.google.protobuf.util.JsonFormat.Printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class Main {

    public static void main(String[] argv) throws IOException {
        List<String> args = new ArrayList<>(asList(argv));
        List<String> paths = new ArrayList<>();

        boolean includeSource = true;
        boolean includeAst = true;
        boolean includePickles = true;
        Printer jsonPrinter = null;
        IdGenerator idGenerator = null;

        while (!args.isEmpty()) {
            String arg = args.remove(0).trim();

            switch (arg) {
                case "--no-source":
                    includeSource = false;
                    break;
                case "--no-ast":
                    includeAst = false;
                    break;
                case "--no-pickles":
                    includePickles = false;
                    break;
                case "--json":
                    jsonPrinter = JsonFormat.printer();
                    break;
                case "--predictable-ids":
                    idGenerator = new IdGenerator.Incrementing();
                    break;
                default:
                    paths.add(arg);
            }
        }

        if (idGenerator == null) {
            idGenerator = new IdGenerator.UUID();
        }

        Stream<Envelope> messages = paths.isEmpty() ?
                Gherkin.fromStream(System.in) :
                Gherkin.fromPaths(paths, includeSource, includeAst, includePickles, idGenerator);
        printMessages(jsonPrinter, messages);
    }

    private static void printMessages(Printer jsonPrinter, Stream<Envelope> messages) {
        messages.forEach(envelope -> {
            try {
                if (jsonPrinter != null) {
                    Stdio.out.write(jsonPrinter.print(envelope));
                    Stdio.out.write("\n");
                    Stdio.out.flush();
                } else {
                    envelope.writeDelimitedTo(System.out);
                }
            } catch (IOException e) {
                throw new GherkinException("Couldn't print messages", e);
            }
        });
    }
}
