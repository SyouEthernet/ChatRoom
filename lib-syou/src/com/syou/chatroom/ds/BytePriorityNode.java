package com.syou.chatroom.ds;

/**
 * node with priority, can construct a linkedList
 * @param <Item>
 */
public class BytePriorityNode<Item> {
    public byte priority;
    public Item item;
    public BytePriorityNode<Item> next;

    public BytePriorityNode(Item item) {
        this.item = item;
    }

    /**
     * add to linkedlist refer to priority
     * @param node
     */
    public void appendWithPriority(BytePriorityNode<Item> node) {
        if (next == null) {
            next = node;
        } else {
            BytePriorityNode<Item> after = this.next;
            if (after.priority < node.priority) {
                // insert
                this.next = node;
                node.next = after;
            } else {
                after.appendWithPriority(node);
            }
        }
    }
}
