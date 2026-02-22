package nur.edu.nurtricenter_patient.core.results;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Error {

    public static final Error NONE = new Error("", "", ErrorType.FAILURE);
    public static final Error NULL_VALUE = new Error(
            "General.Null",
            "Null value was provided",
            ErrorType.FAILURE
    );

    private final String code;
    private final String description;
    private final String structuredMessage;
    private final ErrorType type;

    public Error(String code, String structuredMessage, ErrorType type, Object... args) {
        if (structuredMessage == null) {
            structuredMessage = "";
        }
        this.structuredMessage = structuredMessage;
        this.description = buildMessage(structuredMessage, args);
        this.code = code;
        this.type = type;
    }

    private String buildMessage(String structuredMessage, Object... args) {
        if (args == null || args.length == 0) {
            return structuredMessage;
        }

        String result = structuredMessage;
        Pattern pattern = Pattern.compile("\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(structuredMessage);
        int index = 0;

        while (matcher.find() && index < args.length) {
            String placeholder = matcher.group(0); // Ej: {nombre}
            String value = args[index] != null ? args[index].toString() : "";
            result = result.replace(placeholder, value);
            index++;
        }

        return result;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getStructuredMessage() {
        return structuredMessage;
    }

    public ErrorType getType() {
        return type;
    }

    // Factory methods
    public static Error failure(String code, String structuredMessage, String... args) {
        return new Error(code, structuredMessage, ErrorType.FAILURE, (Object[]) args);
    }

    public static Error notFound(String code, String structuredMessage, String... args) {
        return new Error(code, structuredMessage, ErrorType.NOT_FOUND, (Object[]) args);
    }

    public static Error problem(String code, String structuredMessage, String... args) {
        return new Error(code, structuredMessage, ErrorType.PROBLEM, (Object[]) args);
    }

    public static Error conflict(String code, String structuredMessage, String... args) {
        return new Error(code, structuredMessage, ErrorType.CONFLICT, (Object[]) args);
    }
}
