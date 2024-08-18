package repartidor.faster.com.ec.Getset;

public class paymentGetSet {
    private String isPaid;
    private String paymentNo;
    private String paymentAmount;
    private String orderQuantity;
    private String paymentMaxDate;
    private String paymentDate;
    private String MakePayment;
    private String FreeDelivery;
    private String CommFreeDelivery;

    public String getComplete() {
        return isPaid;
    }

    public void setComplete(String paid) {
        isPaid = paid;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(String paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(String orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getPaymentMaxDate() {
        return paymentMaxDate;
    }

    public void setPaymentMaxDate(String paymentMaxDate) {
        this.paymentMaxDate = paymentMaxDate;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getMakePayment() {
        return MakePayment;
    }

    public void setMakePayment(String MakePayment) {
        this.MakePayment = MakePayment;
    }
    public String getFreeDelivery() {
        return FreeDelivery;
    }
    public void setFreeDelivery(String FreeDelivery) {
        this.FreeDelivery = FreeDelivery;
    }
    public String getCommFreeDelivery() {
        return CommFreeDelivery;
    }
    public void setCommFreeDelivery(String CommFreeDelivery) {
        this.CommFreeDelivery = CommFreeDelivery;
    }
}