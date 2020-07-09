

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class Account
{
    // Флаг блокировки счета
    private AtomicBoolean blocked;
    private AtomicLong money;
    private String accNumber;

    Account(String accNumber, long money){
        this.accNumber = accNumber;
        this.money = new AtomicLong(money);
        // Счет по умолчанию доступен
        this.blocked = new AtomicBoolean(false);
    }

    // метод получения баланса счета
    long getBalance() {
        return money.longValue();
    }

    // метод списания денег со счета
    void getMoney(long count){
        // Понимаю, что в некоторых банках можно уходить в минус, но тут решил сделать без этого.
        money.getAndAdd(-count);
        System.out.println("Произведено списание со счета № " + accNumber + ". Баланс счета: " + getBalance());
    }

    // метод добавления денег на счет
    void addMoney(long count){
        money.getAndAdd(count);
        System.out.println("Произведено пополнение счета № " + accNumber + ". Баланс счета: " + getBalance());
    }



    String getAccNumber() {
        return accNumber;
    }

    boolean isBlocked() {
        return blocked.get();
    }

    void setBlocked(boolean blocked) {
        this.blocked.set(blocked);
    }
}
