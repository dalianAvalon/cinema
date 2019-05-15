$(document).ready(function(){

    var movieId = parseInt(window.location.href.split('?')[1].split('&')[0].split('=')[1]);
    var userId = sessionStorage.getItem('id');
    var isLike = false;

    getMovie();
    if(sessionStorage.getItem('role') === 'admin')
        getMovieLikeChart();

    function getMovieLikeChart() {
       getRequest(
           '/movie/' + movieId + '/like/date',
           function(res){
               var data = res.content,
                    dateArray = [],
                    numberArray = [];
               data.forEach(function (item) {
                   dateArray.push(item.likeTime);
                   numberArray.push(item.likeNum);
               });

               var myChart = echarts.init($("#like-date-chart")[0]);

               // 指定图表的配置项和数据
               var option = {
                   title: {
                       text: '想看人数变化表'
                   },
                   xAxis: {
                       type: 'category',
                       data: dateArray
                   },
                   yAxis: {
                       type: 'value'
                   },
                   series: [{
                       data: numberArray,
                       type: 'line'
                   }]
               };

               // 使用刚指定的配置项和数据显示图表。
               myChart.setOption(option);
           },
           function (error) {
               alert(error);
           }
       );
    }

    function getMovie() {
        getRequest(
            '/movie/'+movieId + '/' + userId,
            function(res){
                var data = res.content;
                isLike = data.islike;
                repaintMovieDetail(data);
            },
            function (error) {
                alert(error);
            }
        );
    }

    function repaintMovieDetail(movie) {
        !isLike ? $('.icon-heart').removeClass('error-text') : $('.icon-heart').addClass('error-text');
        $('#like-btn span').text(isLike ? ' 已想看' : ' 想 看');
        $('#movie-img').attr('src',movie.posterUrl);
        $('#movie-name').text(movie.name);
        $('#order-movie-name').text(movie.name);
        $('#movie-description').text(movie.description);
        $('#movie-startDate').text(new Date(movie.startDate).toLocaleDateString());
        $('#movie-type').text(movie.type);
        $('#movie-country').text(movie.country);
        $('#movie-language').text(movie.language);
        $('#movie-director').text(movie.director);
        $('#movie-starring').text(movie.starring);
        $('#movie-writer').text(movie.screenWriter);
        $('#movie-length').text(movie.length);
    }

    // user界面才有
    $('#like-btn').click(function () {
        var url = isLike ?'/movie/'+ movieId +'/unlike?userId='+ userId :'/movie/'+ movieId +'/like?userId='+ userId;
        postRequest(
             url,
            null,
            function (res) {
                 isLike = !isLike;
                getMovie();
            },
            function (error) {
                alert(error);
            });
    });

    // admin界面才有
    $("#modify-btn").click(function (e) {
        var movie;
        getRequest(
            '/movie/'+movieId + '/' + userId,
            function(res){
                movie = res.content;
                $("#movie-modify-name-input").val(movie.name);
                $("#movie-modify-date-input").val(movie.startDate.slice(0,16));
                $("#movie-modify-img-input").val(movie.posterUrl);
                $("#movie-modify-description-input").val(movie.description);
                $("#movie-modify-type-input").val(movie.type);
                $("#movie-modify-length-input").val(movie.length);
                $("#movie-modify-country-input").val(movie.country);
                $("#movie-modify-language-input").val(movie.language);
                $("#movie-modify-director-input").val(movie.director);
                $("#movie-modify-star-input").val(movie.starring);
                $("#movie-modify-writer-input").val(movie.screenWriter);
                $('#movieModifyModal').modal('show');
                $('#movieModifyModal')[0].dataset.movieId = movie.id;
                console.log(movie);
            },
            function (error) {
                alert(error);
            }
        );


    });


    $("#movie-modify-form-btn").click(function () {
        var form={
            id:movieId,
            name: $('#movie-modify-name-input').val(),
            startDate: $('#movie-modify-date-input').val(),
            posterUrl: $('#movie-modify-img-input').val(),
            description: $('#movie-modify-description-input').val(),
            type: $('#movie-modify-type-input').val(),
            length: $('#movie-modify-length-input').val(),
            country: $('#movie-modify-country-input').val(),
            starring: $('#movie-modify-star-input').val(),
            director: $('#movie-modify-director-input').val(),
            screenWriter: $('#movie-modify-writer-input').val(),
            language: $('#movie-modify-language-input').val()
        }
        if(!validateMovieForm(form)) {
            return;
        }
        postRequest(
            '/movie/update',
            form,
            function (res) {
                if(res.success){
                    getMovie();
                    $("#movieModifyModal").modal('hide');
                }
                else{
                    alert(res.message);
                }
            },
            function (error) {
                alert(JSON.stringify(error));
            });
    });

    function validateMovieForm(data) {
        var isValidate = true;
        if(!data.name) {
            isValidate = false;
            $('#movie-modify-name-input').parent('.form-group').addClass('has-error');
        }
        if(!data.posterUrl) {
            isValidate = false;
            $('#movie-modify-img-input').parent('.form-group').addClass('has-error');
        }
        if(!data.startDate) {
            isValidate = false;
            $('#movie-modify-date-input').parent('.form-group').addClass('has-error');
        }
        return isValidate;
    }



    $("#delete-btn").click(function () {
        var r=confirm("确定下架此电影吗");
        if(r){
            postRequest(
                '/movie/off/batch',
                {movieIdList:[movieId]},
                function(res){
                    if(res.success){
                        getMovie();
                    }else{
                        alert(res.message);
                    }
                },
                function(error){
                    alert(JSON.stringify(error));
                }
            );
        }

    });

});