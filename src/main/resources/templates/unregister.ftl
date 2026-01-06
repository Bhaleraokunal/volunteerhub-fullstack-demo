<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Volunteer Withdrawal Notification</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: #f9f9f9;
            padding: 20px;
            line-height: 1.6;
        }

        .email-container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border: 1px solid #e0e0e0;
            border-radius: 4px;
        }

        .header {
            background-color: #2c3e50;
            padding: 30px;
            border-bottom: 3px solid #34495e;
        }

        .header h1 {
            font-size: 20px;
            font-weight: 500;
            color: #ffffff;
            margin: 0;
        }

        .content {
            padding: 40px 30px;
        }

        .greeting {
            font-size: 16px;
            color: #2c3e50;
            margin-bottom: 24px;
        }

        .message {
            font-size: 15px;
            color: #4a4a4a;
            margin-bottom: 30px;
        }

        .volunteer-name {
            color: #2c3e50;
            font-weight: 600;
        }

        .event-card {
            background-color: #f8f8f8;
            border: 1px solid #e0e0e0;
            padding: 24px;
            margin-bottom: 30px;
        }

        .event-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 0;
        }

        .footer {
            padding: 30px;
            background-color: #f8f8f8;
            border-top: 1px solid #e0e0e0;
        }

        .signature {
            font-size: 15px;
            color: #4a4a4a;
            line-height: 1.8;
        }

        .brand {
            font-weight: 600;
            color: #2c3e50;
        }
    </style>
</head>
<body>
    <div class="email-container">
        <div class="header">
            <h1>Volunteer Withdrawal Notification</h1>
        </div>
        
        <div class="content">
            <p class="greeting">Hello ${organizerName},</p>
            
            <p class="message">
                <span class="volunteer-name">${volunteerName}</span> has withdrawn from your event.
            </p>
            
            <div class="event-card">
                <div class="event-title">${eventName}</div>
            </div>
        </div>
        
        <div class="footer">
            <p class="signature">
                Regards,<br/>
                <span class="brand">VolunteerHub</span>
            </p>
        </div>
    </div>
</body>
</html>