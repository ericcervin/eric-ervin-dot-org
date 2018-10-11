import sqlite3,pystache
from flask import request

def root():
    report_list = list(map(lambda x : {"key" : x[0], "text" : x[1]["title"]}, report_dict.items()))
    report_list = sorted(report_list,key = lambda x: x["text"])
    return pystache.render(root_template, report_list)


root_template = '''
    <!DOCTYPE html>
    <html lang="en">
      <head>
        <title>Philosophy USA</title>
        <style>table,th,td {
               border: 1px solid black;
               border-collapse: collapse;
               padding: 3px;
               text-align: center
 }
          td {text-align: left}
        </style>
      </head>
      <body>
        <div id="header">
          <h1>Philosophy USA</h1>
            <p>Philosophy and religious studies degrees completed during the 2014-2015 academic year.</p>
            <p>Data taken from the Integrated Postsecondary Education Data System (IPEDS)</p>
            <p><a href="https://nces.ed.gov/ipeds/Home/UseTheData">https://nces.ed.gov/ipeds/Home/UseTheData</a></p>
        </div>
        <div id=\"reports\">
          <h4>Reports</h4>
          <table>
          {{#.}}
          <tr><td>{{text}}</td><td><a href=\"/philosophy/reports/{{key}}\">HTML</a></td></tr>
          {{/.}}
        </table>
          </table>
        </div>
      </body>
    </html>
           '''

report_template = '''<!DOCTYPE html>
<html lang="en">
  <head>
    <title>{{title}}</title>
    <style>table,th,td {
          border: 1px solid black;
          border-collapse: collapse;
          padding: 3px;
          text-align: center
       }
          td {text-align: left}
    </style>
  </head>
  <body>
    <h3>{{title}}</h3>
    <table id = 'id_result_table'>
      <thead>
        <tr>{{#header}}<th>{{{.}}}</th>{{/header}}</tr>
      </thead>
      <tbody>
        {{#results}}
        <tr>{{#result}}<td>{{{.}}}</td>{{/result}}</tr>
        {{/results}}
      </tbody>
    </table>
  </body>
</html>'''

def qry_html(qry_dict):
        conn = sqlite3.connect('./resources/philosophy-usa.db')
        c = conn.cursor()
        c.execute(qry_dict["query"])
        all_results = c.fetchall()
        all_results = list(map(lambda x: {"result": x}, all_results))
        title = qry_dict["title"]
        header = qry_dict["header"]
        all_results = {"title": title, "header":header, "results": all_results}
        return pystache.render(report_template,all_results)


report_dict = {
         "state_count" :
              {"title" : "Philosophy Degrees Completed by State",
               "header": ["State", "Count"],
               "query" : '''Select stabbr, sum(all_cnt) as count 
                            from completion cmp 
                            join institution ins on cmp.inst = ins.unitid
                            group by stabbr
                            order by sum(all_cnt) DESC'''},
         "inst_count" :
              {"title": "Philosophy Degrees Completed by Institution",
               "header": ["Institution", "Count"], 
               "query": '''Select instnm, sum(all_cnt) as count 
                           from completion cmp 
                           join institution ins on cmp.inst = ins.unitid
                           group by instnm
                           order by sum(all_cnt) DESC'''},
         "cip_count": 
           {"title" : "Philosophy Degrees Completed by Subject Classification",
            "header" : ["CIP Code", "CIP Title", "Count"], 
            "query" : '''Select cipcode, ciptitle, sum(all_cnt) as count 
                         from completion cmp 
                         join cipcode chp on cmp.cip = chp.cipcode
                         group by cipcode, ciptitle
                         order by sum(all_cnt) DESC''' 
           },
         "awlevel_count": 
           {"title" : "Philosophy Degrees Completed by Award Level",
            "header" : ["Code", "Level", "Count"], 
            "query" : '''Select alcode, alvalue, sum(all_cnt) 
                         from alcode join completion
                         on alcode.alcode = completion.awlevel
                         group by alcode, alvalue'''
           },
          "u_of_w" :
              {"title": "Philosophy Degrees Completed at University of Washington",
               "header": ["Institution", "Degree Level", "Count"], 
               "query": '''Select instnm, alvalue, sum(all_cnt) 
                           from completion cmp 
                           join institution ins on cmp.inst = ins.unitid
                           join alcode on cmp.awlevel = alcode.alcode
                           where instnm LIKE "University of Washington%"
                           and all_cnt > 0
                           group by instnm, alvalue'''}
          
         }
    
def reports(report):
    if report in report_dict:
        return qry_html(report_dict[report])
    else:
        return "<HTML><HEAD></HEAD><BODY>Invalid report name</BODY>"
