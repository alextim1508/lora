package com.alextim.lora.service.protocol;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
    errNoError(0, "Нет ошибки"),
    errNoMemory(1, "Нет свободной памяти"),
    errErrorOfSize(2, "Ошибка в размере"),
    errErrorOfParam(3, "Ошибка в параметре"),
    errNotConnected(4, "Не подсоединен"),
    errOverflow(5, "Ошибка переполнения"),
    errTimeout(6, "Таймаут"),
    errNoInit(7, "Не инициализирован"),
    errName(8, "Ошибка формата имени"),
    errAnswerError(9, "Ошибка в ответе"),
    errAlreadyExists(10, "Уже существует"),
    errIsAbsent(11, "Отсутствует"),
    errInvalidData(12, "Ошибка в данных"),
    errWriteError(13, "Ошибка записи"),
    errReadError(14, "Ошибка чтения"),
    errFalseKS(15, "Ошибка контрольной суммы"),
    errBusy(16, "Занят"),
    errUnknownError(255, "Неизвестная ошибка");

    private final int code;
    private final String title;

    private static final Map<Integer, ErrorCode> CODE_TO_ENUM_MAP = new HashMap<>();
    static {
        for (ErrorCode ec : ErrorCode.values()) {
            CODE_TO_ENUM_MAP.put(ec.getCode(), ec);
        }
    }

    ErrorCode(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static ErrorCode findByCode(int code) {
        return CODE_TO_ENUM_MAP.get(code);
    }

    public static String findTitleByCode(int code) {
        ErrorCode ec = findByCode(code);
        return ec != null ? ec.getTitle() : "неизвестный код ошибки";
    }
}
