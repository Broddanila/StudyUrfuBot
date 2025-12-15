package Handlers;

public class Homework {
    private String subject;
    private String description;
    private int entryOrder;
    public String Get_subject(){
        return subject;
    }
    public String Get_description(){
        return description;
    }
    public int Get_entryOrder(){
        return entryOrder;
    }
    public void Set_subject(String new_subject){
        subject = new_subject;
    }
    public void Set_description(String new_description){
        description = new_description;
    }
    public void Set_entryOrder(int new_entryOrder){
        entryOrder = new_entryOrder;
    }
}
