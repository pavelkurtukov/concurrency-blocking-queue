import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    final static int NUMBER_OF_TEXTS = 10_000;
    final static int TEXT_LENGTH = 100_000;
    final static String TEXT_LETTERS = "abc";

    static BlockingQueue<String> queueA = new ArrayBlockingQueue<>(3);
    static BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    static BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    static AtomicBoolean isGenerating = new AtomicBoolean(false); // Признак работы потока генерации текста

    public static void main(String[] args) throws InterruptedException {

        // Процесс генерации текстов
        System.out.println("Генерируем " + NUMBER_OF_TEXTS + " текстов...");
        isGenerating.set(true);
        Runnable generateTextProcess = () -> {
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                String text = generateText(TEXT_LETTERS, TEXT_LENGTH);
                try {
                    // Кладём текст в 3 очереди
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    isGenerating.set(false);
                    throw new RuntimeException(e);
                }
                //System.out.println("Сгенерирован текст: " + text.substring(0, 99) + "...");
            }
            isGenerating.set(false);
            System.out.println("Генерация завершена\n");
        };

        // Процесс поиска текста с максимальным кол-вом символов "a"
        Runnable findTextWithMaxACount = () -> {
            String textWithMaxA = "";
            int maxACount = 0;
            // Проверяем очередь до тех пор, пока работает процесс генерации текстов и пока не опустеет наполняемая им очередь
            while (isGenerating.get() || !queueA.isEmpty()) {
                try {
                    // Берём текст из очереди
                    String text = queueA.take();

                    // Считаем кол-во символов "a"
                    int aCount = text.length() - text.replace("a", "").length();

                    // Проверяем на максимум кол-ва букв
                    if (aCount > maxACount) {
                        maxACount = aCount;
                        textWithMaxA = text;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            // Выводим результат
            System.out.printf("Текст с наибольшим числом букв \"a\" (%d):\n%s\n\n", maxACount, textWithMaxA.substring(0, 99) + "...");
        };

        // Процесс поиска текста с максимальным кол-вом символов "b"
        Runnable findTextWithMaxBCount = () -> {
            String textWithMaxB = "";
            int maxBCount = 0;
            // Проверяем очередь до тех пор, пока работает процесс генерации текстов и пока не опустеет наполняемая им очередь
            while (isGenerating.get() || !queueB.isEmpty()) {
                try {
                    // Берём текст из очереди
                    String text = queueB.take();

                    // Считаем кол-во символов "a"
                    int bCount = text.length() - text.replace("b", "").length();

                    // Проверяем на максимум кол-ва букв
                    if (bCount > maxBCount) {
                        maxBCount = bCount;
                        textWithMaxB = text;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            // Выводим результат
            System.out.printf("Текст с наибольшим числом букв \"b\" (%d):\n%s\n\n", maxBCount, textWithMaxB.substring(0, 99) + "...");
        };

        // Процесс поиска текста с максимальным кол-вом символов "c"
        Runnable findTextWithMaxCCount = () -> {
            String textWithMaxC = "";
            int maxCCount = 0;
            // Проверяем очередь до тех пор, пока работает процесс генерации текстов и пока не опустеет наполняемая им очередь
            while (isGenerating.get() || !queueC.isEmpty()) {
                try {
                    // Берём текст из очереди
                    String text = queueC.take();

                    // Считаем кол-во символов "a"
                    int cCount = text.length() - text.replace("c", "").length();

                    // Проверяем на максимум кол-ва букв
                    if (cCount > maxCCount) {
                        maxCCount = cCount;
                        textWithMaxC = text;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            // Выводим результат
            System.out.printf("Текст с наибольшим числом букв \"c\" (%d):\n%s\n\n", maxCCount, textWithMaxC.substring(0, 99) + "...");
        };

        // Определяем потоки
        Thread generateTextThread = new Thread(generateTextProcess);
        Thread findTextWinMaxACountThread = new Thread(findTextWithMaxACount);
        Thread findTextWinMaxBCountThread = new Thread(findTextWithMaxBCount);
        Thread findTextWinMaxCCountThread = new Thread(findTextWithMaxCCount);

        // Стартуем потоки
        generateTextThread.start();
        findTextWinMaxACountThread.start();
        findTextWinMaxBCountThread.start();
        findTextWinMaxCCountThread.start();

        // Ждём окончания всех потоков
        generateTextThread.join();
        findTextWinMaxACountThread.join();
        findTextWinMaxBCountThread.join();
        findTextWinMaxCCountThread.join();
    }

    // Генератор текстов
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
