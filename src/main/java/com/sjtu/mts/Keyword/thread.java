package com.sjtu.mts.Keyword;


public class thread extends Thread {
    public thread(String name){
        super(name);
    }
    @Override
    public void run() {
        for(int i=0;i<100;i++){
            for(long k=0;k<100000000;k++);
            System.out.println(this.getName()+":"+i);
        }
    }

    public int rettt(){
        Thread t1=new thread("李白");
        Thread t2=new thread("屈原");
        t1.start();
        t2.start();
        for(int i=0;i<5;i++){
            for(long k=0;k<100000000;k++);
            System.out.println(":"+i);
        }
        return 1;
    }

    public static void main(String[] args){
        thread thread1=new thread("ll");
        int a=thread1.rettt();
        System.out.println(a);
    }
}