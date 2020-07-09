import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.junit.Before;

import java.util.HashMap;
import java.util.Random;

public class BankTest extends TestCase {
    private static final Logger logger = LogManager.getLogger(TestCase.class);
    private static final Marker INFO_LOG = MarkerManager.getMarker("INFO");
    // Количество потоков для теста
    private final int threadCount = 100;
    // Сумма баланса всех счетов банка.
    private long allAccountBalance;
    private HashMap<String, Account> accountHashMap = new HashMap<>();
    private Bank testBank = new Bank();
    // Создаем аккаунт для теста корректной работы в многопоточной среде.
    private Account testAccount = new Account("test", 50000);
    @Before
    @Override
    public void setUp(){
        // Для чистоты эксперемента генерируем случайный баланс счета
        Random random = new Random();
        // Задаем счету диапазон от 1 до 1000000
        int diapason = 1000000;
        // Создаем аккаунты для теста переводов в многопоточной среде
        accountHashMap.put("1", new Account("1", random.nextInt(diapason) + 1));
        accountHashMap.put("2", new Account("2", random.nextInt(diapason) + 1));
        accountHashMap.put("3", new Account("3", random.nextInt(diapason) + 1));
        accountHashMap.put("4", new Account("4", random.nextInt(diapason) + 1));
        accountHashMap.put("5", new Account("5", random.nextInt(diapason) + 1));
        accountHashMap.put("6", new Account("6", random.nextInt(diapason) + 1));
        accountHashMap.put("7", new Account("7", random.nextInt(diapason) + 1));
        accountHashMap.put("8", new Account("8", random.nextInt(diapason) + 1));
        accountHashMap.put("9", new Account("9", random.nextInt(diapason) + 1));
        // C этого аккаунта будем совершать подозрительные переводы
        accountHashMap.put("10", new Account("10", diapason));
        // Добавляем счета банку
        testBank.setAccounts(accountHashMap);
        // Считаем сумму баланса
        allAccountBalance = testBank.getAccounts().values().stream().mapToLong(Account::getBalance).sum();
    }
    // Тест работы счетов в однопоточном режиме
    public void testWorkAccount(){
        logger.info(INFO_LOG, "Work account test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        long expected = 20000;
        for (int i = 0; i < 50; i++) {
            //В процессе списания прибавляем 10000 к счету
            if(i > 20 && i <= 30){
                testAccount.addMoney(1000);
            }else {
                testAccount.getMoney(1000);
            }
        }
        long actual = testAccount.getBalance();

        logging(expected, actual);
        assertEquals(expected, actual);
    }
    // Тест работы счетов в многопоточном режиме
    public void testMultithreadingWorkAccount() throws InterruptedException {
        logger.info(INFO_LOG, "Multithreading work account test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        long expected = 20000;
        for (int i = 0; i < 50; i++) {
            //В процессе списания прибавляем 10000 к счету
            if(i > 20 && i <= 30){
                new Thread(()-> testAccount.addMoney(1000)).start();
            }else {
                new Thread(()-> testAccount.getMoney(1000)).start();
            }
        }
        // Даем потоку время для отработке всех действий.
        Thread.sleep(3000);
        long actual = testAccount.getBalance();
        logging(expected, actual);
        assertEquals(expected, actual);
    }

    // Тест работы переводов в однопоточном режиме (на потерю денег)
    public void testTransferWork() {
        logger.info(INFO_LOG, "Transfer test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        // На всех счетах находится 190000
        long expected = allAccountBalance;
        testBank.transfer("1", "6", 1000);
        testBank.transfer("2", "7", 1000);
        testBank.transfer("3", "8", 1000);
        testBank.transfer("4", "9", 1000);
        testBank.transfer("1", "10", 1000);
        long actual = testBank.getAccounts().values().stream().mapToLong(Account::getBalance).sum();

        logging(expected, actual);
        assertEquals(expected, actual);
    }

    // Тест работы переводов в многопоточном режиме (на потерю денег)
    public void testMultithreadingWorkTransfer() throws InterruptedException {
        logger.info(INFO_LOG, "Multithreading transfer test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        long expected = allAccountBalance;
        for (int i = 0; i < threadCount; i++) {
            transferThread("1", "6", 1000);
            transferThread("2", "7", 1000);
            transferThread("3", "8", 1000);
            transferThread("4", "9", 1000);
            transferThread("1", "10", 1000);
            transferThread("5", "4", 1000);
            transferThread("6", "5", 1000);
            transferThread("7", "6", 1000);
            transferThread("8", "7", 1000);
            transferThread("9", "8", 1000);
        }
        long actual = testBank.getAccounts().values().stream().mapToLong(Account::getBalance).sum();

        logging(expected, actual);
        assertEquals(expected, actual);
    }

    public void testBlockFlag() throws InterruptedException {
        logger.info(INFO_LOG, "Flag test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        for (int i = 0; i < threadCount; i++) {
            transferThread("10", "8", 51000);
        }
        boolean actual = testBank.getAccounts().get("10").isBlocked();

        logger.info(INFO_LOG, "Expected account flag: " + true + ". Actual flag: " + actual + ".");
        if(actual){
            logger.info(INFO_LOG, "The test was passed successfully.");
        } else {
            logger.warn(INFO_LOG, "The test failed.");
        }
        logger.info(INFO_LOG, "The test is complete.\n\n");

        System.out.println("Счет № 8 заблокирован? : " + testBank.getAccounts().get("8").isBlocked());
        assertTrue(actual);
    }

    public void testAllProgram() throws InterruptedException {
        logger.info(INFO_LOG, "All program test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        long expected = allAccountBalance;
        Random random = new Random();

        for (int i = 0; i < threadCount; i++) {
            int randomAccFrom = random.nextInt(10) + 1;
            int randomAccTo = random.nextInt(10) + 1;
            long randomAmount = random.nextInt(60000) + 1;
            transferThread(String.valueOf(randomAccFrom), String.valueOf(randomAccTo), randomAmount);
        }
        long actual = testBank.getAccounts().values().stream().mapToLong(Account::getBalance).sum();

        logging(expected, actual);
        assertEquals(expected, actual);
    }

    public void testFinal() throws InterruptedException {
        logger.info(INFO_LOG, "Final program test is running.");
        logger.info(INFO_LOG, "Balance of all accounts: " + allAccountBalance);
        long expected = allAccountBalance;
        for (int i = 0; i < threadCount; i++) {
                transferThread("1", "6", 1);
                transferThread("1", "6", 1);
                transferThread("1", "6", 1);
                transferThread("1", "6", 1);
                transferThread("1", "6", 1);
                transferThread("6", "1", 1);
                transferThread("6", "1", 1);
                transferThread("6", "1", 1);
                transferThread("6", "1", 1);
                transferThread("6", "1", 1);
        }

        long actual = testBank.getAccounts().values().stream().mapToLong(Account::getBalance).sum();

        logging(expected, actual);
        assertEquals(expected, actual);
    }

    private void transferThread(String from, String to, long amount) throws InterruptedException {
        Thread thread = new Thread(()-> testBank.transfer(from, to, amount));
        thread.start();
        thread.join();
    }

    // Метод лагирования
    private void logging(long expected, long actual){
        logger.info(INFO_LOG, "Expected account balance: " + expected + ". Actual balance: " + actual + ".");
        if(expected == actual){
            logger.info(INFO_LOG, "The test was passed successfully.");
        } else {
            logger.warn(INFO_LOG, "The test failed.");
        }
        logger.info(INFO_LOG, "The test is complete.\n\n");
    }
}
