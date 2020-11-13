package io.github.shinglem.util.consoletable.converter;


import io.github.shinglem.util.consoletable.Bordered;
import io.github.shinglem.util.consoletable.Converter;
import io.github.shinglem.util.consoletable.PrettyTable;
import kotlin.text.StringsKt;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public abstract class ConsoleConverter implements Converter, Bordered {

    private boolean border;

    @Override
    public ConsoleConverter border(final boolean border) {
        this.border = border;
        return this;
    }

    abstract ConsoleConverter clear();

    abstract ConsoleConverter af(String text);

    abstract ConsoleConverter ab(String text);

    @Override
    public String convert(PrettyTable pt) {

        clear();

        // Check empty
        if (pt.fieldNames.isEmpty()) {
            return "";
        }

        int[] maxWidth = adjustMaxWidth(pt);

        topBorderLine(maxWidth);

        leftBorder();

        for (int i = 0; i < pt.fieldNames.size(); i++) {
            af(StringsKt.padEnd(pt.fieldNames.get(i), maxWidth[i], ' '));

            if (i < pt.fieldNames.size() - 1) {
                centerBorder();
            } else {
                rightBorder();
            }
        }

        bottomBorderLine(maxWidth);

        // Convert rows to table
        pt.rows.forEach(r -> {
            ab("\n");
            leftBorder();

            for (int c = 0; c < r.length; c++) {

                String nc;
                if (r[c] instanceof Number) {
                    String n = pt.comma
                            ? NumberFormat
                            .getNumberInstance(Locale.US)
                            .format(r[c])
                            : r[c].toString();
                    nc = StringsKt.padStart(n, maxWidth[c], ' ');
                } else {
                    if (r[c] == null) {
                        nc = StringsKt.padEnd("null", maxWidth[c], ' ');
                    } else {
                        nc = StringsKt.padEnd(r[c].toString(), maxWidth[c], ' ');
                    }
                }

                af(nc);

                if (c < r.length - 1) {
                    centerBorder();
                } else {
                    rightBorder();
                }
            }
        });

        bottomBorderLine(maxWidth);

        return toString();
    }

    /*
     * Adjust for max width of the column
     */
    public int[] adjustMaxWidth(PrettyTable pt) {

        // Adjust comma
        List<List<String>> converted = pt.rows.stream()
                .map(r -> Stream.of(r).map(o -> {
                    if (pt.comma && o instanceof Number) {
                        return NumberFormat
                                .getNumberInstance(Locale.US)
                                .format(o);
                    } else {
                        if (o == null) {
                            return "null";
                        }
                        return o.toString();
                    }
                }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        return IntStream.range(0, pt.fieldNames.size())
                .map(i -> {
                    int n = converted.stream()
                            .map(f -> f.get(i).length())
                            .max(Comparator.naturalOrder())
                            .orElse(0);
                    return Math.max(pt.fieldNames.get(i).length(), n);
                }).toArray();
    }

    private ConsoleConverter topBorderLine(final int[] maxWidth) {
        ab(border ? line(maxWidth) + "\n" : "");
        return this;
    }

    private ConsoleConverter bottomBorderLine(final int[] maxWidth) {
        ab(border ? "\n" + line(maxWidth) : "");
        return this;
    }

    private ConsoleConverter leftBorder() {
        ab(border ? "| " : "");
        return this;
    }

    private ConsoleConverter rightBorder() {
        ab(border ? " |" : "");
        return this;
    }

    private ConsoleConverter centerBorder() {
        ab(border ? " | " : " ");
        return this;
    }

    private static String line(final int[] maxWidth) {

        final StringBuilder sb = new StringBuilder();

        sb.append("+");
        for (int i = 0; i < maxWidth.length; i++) {
            sb.append(StringsKt.padEnd("", maxWidth[i] + 2, '-'));
            sb.append("+");
        }
        return sb.toString();
    }
}
