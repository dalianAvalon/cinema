package com.example.cinema.blImpl.sales;

import com.example.cinema.bl.sales.TicketService;
import com.example.cinema.blImpl.management.hall.HallServiceForBl;
import com.example.cinema.blImpl.management.schedule.ScheduleServiceForBl;
import com.example.cinema.data.promotion.CouponMapper;//
import com.example.cinema.data.promotion.VIPCardMapper;//
import com.example.cinema.data.sales.TicketMapper;
import com.example.cinema.data.promotion.ActivityMapper;
import com.example.cinema.po.*;
import com.example.cinema.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liying on 2019/4/16.
 */
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    ScheduleServiceForBl scheduleService;
    @Autowired
    HallServiceForBl hallService;
    Ticket ticket;
    Coupon coupon;
    CouponMapper couponMapper;
    VIPCardMapper vipCardMapper;
    ActivityMapper activityMapper;

    VIPCard vipCard;
    AudiencePrice audiencePrice;


    @Override
    @Transactional
    public ResponseVO addTicket(TicketForm ticketForm) {
        try{
            List<SeatForm> seats=ticketForm.getSeats();
            List<Ticket> tickets=new ArrayList<Ticket>();
            for (int i=0;i<seats.size();i++){
                SeatForm item=seats.get(i);
                ticket.setUserId(ticketForm.getUserId());
                ticket.setScheduleId(ticketForm.getScheduleId());
                ticket.setRowIndex(item.getRowIndex());
                ticket.setColumnIndex(item.getColumnIndex());
                ticket.setState(0);
                tickets.add(ticket);
            }

            if(tickets.size()==1){
                ticketMapper.insertTicket(tickets.get(0));
            }
            else{
                ticketMapper.insertTickets(tickets);
            }
            return ResponseVO.buildSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    @Transactional
    public ResponseVO completeTicket(List<Integer> id, int couponId) {
        try{
            ticket=ticketMapper.selectTicketById(id.get(0));
            int userId=ticket.getUserId();
            audiencePrice.setUserId(userId);
            int scheduleId=ticket.getScheduleId();
            ScheduleItem scheduleItem=scheduleService.getScheduleItemById(scheduleId);
            double fare=scheduleItem.getFare();

            coupon=couponMapper.selectById(couponId);
            double total=id.size()*fare;
            if(coupon!=null) {
                double targetAmount = coupon.getTargetAmount();
                double discountAmount = coupon.getDiscountAmount();
                if (targetAmount <= fare) {
                    total = total - discountAmount;
                }
            }


            audiencePrice.setTotalPrice(total);
            for(int ticketId:id){
                ticketMapper.updateTicketState(ticketId,1);
            }

            return ResponseVO.buildSuccess();

        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getBySchedule(int scheduleId) {
        try {
            List<Ticket> tickets = ticketMapper.selectTicketsBySchedule(scheduleId);
            ScheduleItem schedule=scheduleService.getScheduleItemById(scheduleId);
            Hall hall=hallService.getHallById(schedule.getHallId());
            int[][] seats=new int[hall.getRow()][hall.getColumn()];
            tickets.stream().forEach(ticket -> {
                seats[ticket.getRowIndex()][ticket.getColumnIndex()]=1;
            });
            ScheduleWithSeatVO scheduleWithSeatVO=new ScheduleWithSeatVO();
            scheduleWithSeatVO.setScheduleItem(schedule);
            scheduleWithSeatVO.setSeats(seats);
            return ResponseVO.buildSuccess(scheduleWithSeatVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTicketByUser(int userId) {
        try{
            List<Ticket> tickets=ticketMapper.selectTicketByUser(userId);
            if(tickets!=null){
                for(int i=0;i<tickets.size();i++){
                    ticket=tickets.get(i);

                }return ResponseVO.buildSuccess(new TicketVO());

            }
            else{
                return ResponseVO.buildSuccess(null);
            }

        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    @Transactional
    public ResponseVO completeByVIPCard(List<Integer> id, int couponId) {
        try{
            ticket=ticketMapper.selectTicketById(id.get(0));
            int userId=ticket.getUserId();
            audiencePrice.setUserId(userId);
            int scheduleId=ticket.getScheduleId();
            ScheduleItem scheduleItem=scheduleService.getScheduleItemById(scheduleId);
            int movieId=scheduleItem.getMovieId();
            double fare=scheduleItem.getFare();
            vipCard=vipCardMapper.selectCardByUserId(userId);
            double balance=vipCard.getBalance();
            double total=id.size()*fare;

            coupon=couponMapper.selectById(couponId);
            if(coupon!=null){
                double targetAmount=coupon.getTargetAmount();
                double discountAmount=coupon.getDiscountAmount();
                if(targetAmount<=fare){
                    total=total-discountAmount;
                }
            }

            total=balance-total;
            audiencePrice.setTotalPrice(total);
            vipCard.setBalance(total);
            for(int ticketId:id){
                ticketMapper.updateTicketState(ticketId,1);
            }


            List<Activity> activities=activityMapper.selectActivitiesByMovie(movieId);
            for(Activity activity:activities){
                coupon=activity.getCoupon();
                couponMapper.insertCoupon(coupon);
            }
            List<Activity> activities_2=activityMapper.selectActivitiesWithoutMovie();
            for(Activity activity:activities_2){
                coupon=activity.getCoupon();
                couponMapper.insertCoupon(coupon);
            }

            return ResponseVO.buildSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO cancelTicket(List<Integer> id) {
        try{
            for(int i=0;i<id.size();i++){
                int ticketId=id.get(i);
                ticket=ticketMapper.selectTicketById(ticketId);
                if(ticket.getState()==0){
                    ticketMapper.deleteTicket(ticketId);
                }

            }return ResponseVO.buildSuccess();
        }catch(Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }



}
