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

        // Процессы поиска текстов с максимальным кол-вом символов
        Runnable findTextWithMaxACount = () -> findTextWithMaxLetterCount(queueA, 'a');
        Runnable findTextWithMaxBCount = () -> findTextWithMaxLetterCount(queueB, 'b');
        Runnable findTextWithMaxCCount = () -> findTextWithMaxLetterCount(queueC, 'c');

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

    // Процесс поиска текста с максимальным кол-вом указанного символа
    static void findTextWithMaxLetterCount(BlockingQueue<String> queue, Character letter) {
        String textWithMaxLetter = "";
        int maxLetterCount = 0;
        // Проверяем очередь до тех пор, пока работает процесс генерации текстов и пока не опустеет наполняемая им очередь
        while (isGenerating.get() || !queue.isEmpty()) {
            try {
                // Берём текст из очереди
                String text = queue.take();

                // Считаем кол-во символов
                int letterCount = text.length() - text.replace(letter.toString(), "").length();

                // Проверяем на максимум кол-ва букв
                if (letterCount > maxLetterCount) {
                    maxLetterCount = letterCount;
                    textWithMaxLetter = text;
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        // Выводим результат
        System.out.printf("Текст с наибольшим числом букв \"%c\" (%d):\n%s\n\n",
                letter, maxLetterCount, textWithMaxLetter.substring(0, 99) + "...");
    };
}
