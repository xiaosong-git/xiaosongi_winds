<html>
<body>

<h2>Hello World!</h2>
</body>
<center>
<form action="/device" method="post">
    <%--<input  type="text" name="page"><br>--%>
    <%--<input type="submit"/>--%>
        <div class="layui-form-item">

            <label class="layui-form-label">项点名称：</label>

            <div class="layui-input-block">

                <input type="text" name="ca.name" required

                       placeholder="请输入名称" autocomplete="off" class="layui-input">

            </div>

        </div>

        <div class="layui-form-item">

            <label class="layui-form-label">车号：</label>

            <div class="layui-input-block">

                <select id="select_number" name="ca.c_number_id" lay-filter="role" >

                    <option value=""></option>

                    #for(x : list)

                    <option value="#(x.id)">#(x.c_number)</option>

                    #end

                </select>

            </div>

        </div>



        <div class="layui-form-item">

            <label class="layui-form-label">项点描述：</label>

            <div class="layui-input-block">

                <textarea  name="ca.descirption" placeholder="请输入描述" class="layui-textarea"></textarea>

            </div>

        </div>

        <div class="layui-form-item">

            <label class="layui-form-label">上传图片：</label>

            <input  type="file" id="chooseImage" name="file" width= 300px>

            <img id="cropedBigImg" value='custom'  data-address='' height=80px margen-top:10px/>

        </div>

        <div class="layui-form-item">

            <div class="layui-input-block">

                <button class="layui-btn" lay-submit lay-filter="form-btn" >立即提交</button>

                <button type="reset" class="layui-btn layui-btn-primary">重置</button>

            </div>

        </div>
</form>
</center>
</html>
