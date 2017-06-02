

select s3.timeIndexInSeconds
from strava_streams s3 
where s3.activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
  and s3.timeIndexInSeconds <= (select max(s4.timeIndexInSeconds) - 4 from strava_streams s4 where s4.activityId = s3.activityId limit 10)
limit 10

select 
(select s3.timeIndexInSeconds
from strava_streams s3 
where s3.activityId = s1.activityId
  and s3.timeIndexInSeconds <= (select max(s4.timeIndexInSeconds) - 4 from strava_streams s4 where s4.activityId = s1.activityId)) as foo
from strava_streams s1
where s1.activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
limit 10

alter table strava_streams add index idx_activityId_timeIndexInSeconds (activityId, timeIndexInSeconds)
alter table strava_streams add index idx_timeIndexInSeconds (timeIndexInSeconds)
alter table strava_streams drop index idx_activityId_timeIndexInSeconds

select max(avg_watts)
from (select 
      s1.timeIndexInSeconds, 
      ( select avg(watts) as avg_watts
        from strava_streams s2 
        where s2.timeIndexInSeconds between s1.timeIndexInSeconds and s1.timeIndexInSeconds + 4
          and s2.activityId = s1.activityId) as avg_watts,
      ( select count(s2.id) as row_count
        from strava_streams s2 
        where s2.timeIndexInSeconds between s1.timeIndexInSeconds and s1.timeIndexInSeconds + 4
          and s2.activityId = s1.activityId) as row_count
      from strava_streams s1
      where s1.activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
      order by s1.timeIndexInSeconds) as agg

select count(*) from strava_streams where activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
select max(timeIndexInSeconds) from strava_streams where activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
      
select timeIndexInSeconds, watts
from strava_streams
where activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
order by timeIndexInSeconds
limit 15;

select timeIndexInSeconds, watts
from strava_streams
where activityId = '65604a9c-7a45-4685-a1cd-65c96dc568f1'
  and timeIndexInSeconds between 0 and 4
order by timeIndexInSeconds
limit 10;


1
2
3
4
5
6
7
8
9
10

1,2 2,3 3,4 4,5, 5,6, 6,7, 7,8, 8,9, 9,10

1,2,3 | 2,3,4 | 3,4,5 | 4,5,6 | 5,6,7 | 6,7,8 | 7,8,9 | 8,9,10 | 
