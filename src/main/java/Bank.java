import java.util.HashMap;
import java.util.Random;

public class Bank
{
    private HashMap<String, Account> accounts;
    private final Random random = new Random();
    private static final int checkAmount = 50000;
    private static String testString = "";

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException
    {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    void transfer(String fromAccountNum, String toAccountNum, long amount) {
        boolean fraud;
        // Если переводим на тот же счет с которого списываем
        if (fromAccountNum.equals(toAccountNum)) {
            System.out.println("Операция бессмыслена");
            return;
        }

        Account lowSyncAccount = accounts.get(fromAccountNum);
        Account topSyncAccount = accounts.get(toAccountNum);
        if(lowSyncAccount.getAccNumber().compareTo(topSyncAccount.getAccNumber()) > 0){
            topSyncAccount = accounts.get(fromAccountNum);
            lowSyncAccount = accounts.get(toAccountNum);
        }

        synchronized (topSyncAccount) {
            synchronized (lowSyncAccount) {
                // Если любой из счетов уже был заблокирован раньше
                if (topSyncAccount.isBlocked()) {
                    System.out.println("Счет № " + topSyncAccount.getAccNumber() + " уже был заблокирован ранее.");
                    return;
                }
                if (lowSyncAccount.isBlocked()) {
                    System.out.println("Счет № " + lowSyncAccount.getAccNumber() + " уже был заблокирован ранее.");
                    return;
                }

                // Создаем пару аккаунтов для перевода
                Account fromAccountToTransfer = accounts.get(fromAccountNum);
                Account toAccountToTransfer = accounts.get(toAccountNum);

                // Если перевод > 50000к
                if (amount > checkAmount) {
                    // Проверка на мошенничество
                    try {
                        fraud = isFraud(fromAccountNum, toAccountNum, amount);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fraud = true;
                    }
                    // Если мошенничество то блочим акки
                    if (fraud) {
                        topSyncAccount.setBlocked(true);
                        lowSyncAccount.setBlocked(true);
                        System.out.println("Выявлено мошенничество!!! Счет " + fromAccountNum +
                                " и счет " + toAccountNum + " заблокированы!");
                    } else {
                        transferAction(fromAccountToTransfer, toAccountToTransfer, amount);
                    }
                } else {
                    transferAction(fromAccountToTransfer, toAccountToTransfer, amount);
                }
            }
        }
    }

    // Проверял, метод работает (вроде бы) просто в своей реализации он мне не понадобился
    public long getBalance(String accountNum)
    {
       return accounts.get(accountNum).getBalance();
    }

    HashMap<String, Account> getAccounts() {
        return accounts;
    }

    void setAccounts(HashMap<String, Account> accounts) {
        this.accounts = accounts;
    }

    private void transferAction(Account from, Account to, long amount){
        if (from.isBlocked()) {
            System.out.println("Счет " + from.getAccNumber() + " заблокирован. Перевод невозможен.");
            return;
        }
        if(to.isBlocked()){
            System.out.println("Счет " + to.getAccNumber() + " заблокирован. Перевод невозможен.");
            return;
        }
        if ((from.getBalance() - amount) >= 0) {
            from.getMoney(amount);
            to.addMoney(amount);
            System.out.println("Перевод " + amount + " у.е. со счета " + from.getAccNumber() +
                    " на счет " + to.getAccNumber() + " успешно совершен.");
        } else {
            System.out.println("На счете недостаточно денег для перевода.");
        }
    }
}
