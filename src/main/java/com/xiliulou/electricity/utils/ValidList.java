package com.xiliulou.electricity.utils;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @Author SongJinpan
 * @description: 请求参数对象校验集合
 * @Date 2024/3/21 9:16
 */
public class ValidList<E> implements List<E> {
    
    @Valid
    private List<E> list = new LinkedList<>();
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public boolean contains(Object object) {
        return list.contains(object);
    }
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return list.toArray();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }
    
    @Override
    public boolean add(E e) {
        return list.add(e);
    }
    
    @Override
    public boolean remove(Object object) {
        return list.remove(object);
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }
    
    @Override
    public void clear() {
        list.clear();
    }
    
    @Override
    public E get(int index) {
        return list.get(index);
    }
    
    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }
    
    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }
    
    @Override
    public E remove(int index) {
        return list.remove(index);
    }
    
    @Override
    public int indexOf(Object object) {
        return list.indexOf(object);
    }
    
    @Override
    public int lastIndexOf(Object object) {
        return list.lastIndexOf(object);
    }
    
    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
    
    public void setList(List<E> list) {
        this.list = list;
    }
    
    public List<E> getList() {
        return list;
    }
}
